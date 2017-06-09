/******************************************************************
 * CyberHTTP for Java
 * <p>
 * Copyright (C) Satoshi Konno 2002-2004
 * <p>
 * File: HTTPSocket.java
 * <p>
 * Revision;
 * <p>
 * 12/12/02
 * - first revision.
 * 03/11/04
 * - Added the following methods about chunk size.
 * setChunkSize(), getChunkSize().
 * 08/26/04
 * - Added a isOnlyHeader to post().
 * 03/02/05
 * - Changed post() to suppot chunked stream.
 * 06/10/05
 * - Changed post() to add a Date headedr to the HTTPResponse before the posting.
 * 07/07/05
 * - Lee Peik Feng <pflee@users.sourceforge.CyberGarage.net>
 * - Fixed post() to output the chunk size as a hex string.
 ******************************************************************/

package cn.ysu.edu.realtimeshare.httpserver.http;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.RunnableFuture;


public class HTTPSocket {

    private static final String TAG = "HTTPSocket";

    // //////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////

    /**
     * @param socket
     **/
    public HTTPSocket(Socket socket) {
        setSocket(socket);
        open();
    }

    /**
     * @param socket
     */
    public HTTPSocket(HTTPSocket socket) {
        setSocket(socket.getSocket());
        setInputStream(socket.getInputStream());
        setOutputStream(socket.getOutputStream());
    }

    @Override
    public void finalize() {
        close();
    }

    // //////////////////////////////////////////////
    // Socket
    // //////////////////////////////////////////////

    /**
     * Socket socket
     */
    private Socket socket = null;

    private void setSocket(Socket socket) {
        this.socket = socket;
    }


    public Socket getSocket() {
        return socket;
    }

    // //////////////////////////////////////////////
    // local address/port
    // //////////////////////////////////////////////

    public String getLocalAddress() {
        return getSocket().getLocalAddress().getHostAddress();
    }

    public int getLocalPort() {
        return getSocket().getLocalPort();
    }

    // //////////////////////////////////////////////
    // in/out
    // //////////////////////////////////////////////

    private InputStream sockIn = null;

    private OutputStream sockOut = null;

    private void setInputStream(InputStream in) {
        sockIn = in;
    }

    public InputStream getInputStream() {
        return sockIn;
    }

    private void setOutputStream(OutputStream out) {
        sockOut = out;
    }

    private OutputStream getOutputStream() {
        return sockOut;
    }

    // //////////////////////////////////////////////
    // open/close
    // //////////////////////////////////////////////

    public boolean open() {
        Socket sock = getSocket();
        try {
            sockIn = sock.getInputStream();

            sockOut = sock.getOutputStream();
        } catch (Exception e) {
            // TODO Add blacklistening of the UPnP Device
            return false;
        }
        return true;
    }

    public boolean close() {
        try {
            if (sockIn != null) {
                sockIn.close();
            }
            if (sockOut != null) {
                sockOut.close();
            }
            getSocket().close();
        } catch (Exception e) {
            // Debug.warning(e);
            return false;
        }
        return true;
    }

    // //////////////////////////////////////////////
    // post
    // //////////////////////////////////////////////

    /**
     * ������д��
     *
     * @param httpRes
     * @param content
     * @param contentOffset
     * @param contentLength
     * @param isOnlyHeader
     **/
    private boolean post(HTTPResponse httpRes, byte content[],
                         long contentOffset, long contentLength, boolean isOnlyHeader) {
        // TODO Check for bad HTTP agents, this method may be list for
        // IOInteruptedException and for blacklistening
        httpRes.setDate(Calendar.getInstance());

        OutputStream out = getOutputStream();

        try {
            httpRes.setContentLength(contentLength);

            out.write(httpRes.getHeader().getBytes());

            out.write(HTTP.CRLF.getBytes());

            if (isOnlyHeader == true) {
                out.flush();
                return true;
            }

            boolean isChunkedResponse = httpRes.isChunked();

            if (isChunkedResponse == true) {
                // Thanks for Lee Peik Feng <pflee@users.sourceforge.CyberGarage.net>
                // (07/07/05)
                String chunSizeBuf = Long.toHexString(contentLength);

                out.write(chunSizeBuf.getBytes());

                out.write(HTTP.CRLF.getBytes());
            }

            out.write(content, (int) contentOffset, (int) contentLength);

            if (isChunkedResponse == true) {
                out.write(HTTP.CRLF.getBytes());

                out.write("0".getBytes());

                out.write(HTTP.CRLF.getBytes());
            }

            out.flush();
        } catch (Exception e) {
            // Debug.warning(e);
            return false;
        }

        /**
         *  fix in 2017/4/25 by BasinPei
         */
        try {
            this.getSocket().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * @param httpRes
     * @param in
     * @param contentOffset
     * @param contentLength
     * @param isOnlyHeader
     * @return
     */
    private boolean post(final HTTPResponse httpRes, final InputStream in,
                         final long contentOffset, final long contentLength, final boolean isOnlyHeader) {
        // TODO Check for bad HTTP agents, this method may be list for

        try {
            httpRes.setDate(Calendar.getInstance());

            OutputStream out = getOutputStream();

            httpRes.setContentLength(contentLength);


            out.write(httpRes.getHeader().getBytes());

            out.write(HTTP.CRLF.getBytes());

            if (isOnlyHeader == true) {
                out.flush();
                return true;
            }

            boolean isChunkedResponse = httpRes.isChunked();

            if (0 < contentOffset) {
                in.skip(contentOffset);
            }

            int chunkSize = HTTP.getChunkSize();

            byte readBuf[] = new byte[chunkSize];
            long readCnt = 0;
            long readSize = (chunkSize < contentLength) ? chunkSize
                    : contentLength;
            int readLen = in.read(readBuf, 0, (int) readSize);
            Thread current = Thread.currentThread();
            while (0 < readLen && readCnt < contentLength) {
                if (isChunkedResponse == true) {
                    // Thanks for Lee Peik Feng <pflee@users.sourceforge.CyberGarage.net>
                    // (07/07/05)
                    String chunSizeBuf = Long.toHexString(readLen);
                    out.write(chunSizeBuf.getBytes());
                    out.write(HTTP.CRLF.getBytes());
                }

                Thread thread = Thread.currentThread();
                Log.d(TAG, "post: " + thread.equals(current));
                //// TODO: 2017/5/16  thread on pause another thread
                out.write(readBuf, 0, readLen);

                if (isChunkedResponse == true) {
                    out.write(HTTP.CRLF.getBytes());
                }
                readCnt += readLen;
                readSize = (chunkSize < (contentLength - readCnt)) ? chunkSize
                        : (contentLength - readCnt);
                readLen = in.read(readBuf, 0, (int) readSize);
                //add in 2017/05/21 By BasinPei
                out.flush();
            }


            if (isChunkedResponse == true) {
                out.write("0".getBytes());
                out.write(HTTP.CRLF.getBytes());
            }

            out.flush();

            /**
             * 	add in 2017/4/29 by BasinPei
             */
            HTTPSocket.this.getSocket().close();
        } catch (IOException e) {
            return false;
        }


        return true;
    }

    /**
     * @param httpRes
     * @param contentOffset
     * @param contentLength
     * @param isOnlyHeader
     * @return
     */
    public boolean post(HTTPResponse httpRes, long contentOffset,
                        long contentLength, boolean isOnlyHeader) {
        // TODO Close if Connection != keep-alive
        if (httpRes.hasContentInputStream() == true) {
            return post(httpRes, httpRes.getContentInputStream(),
                    contentOffset, contentLength, isOnlyHeader);
        }
        return post(httpRes, httpRes.getContent(), contentOffset,
                contentLength, isOnlyHeader);
    }
}
