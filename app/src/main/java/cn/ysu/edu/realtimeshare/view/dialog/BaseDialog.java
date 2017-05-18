package cn.ysu.edu.realtimeshare.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;

/**
 * Created by BasinPei on 2017/4/23.
 */
public abstract class BaseDialog {

    protected Context _context;

    protected Dialog _dialog;

    protected AlertDialog.Builder _build;

    protected View _contentView;

    public BaseDialog(Context context)
    {
        _context=context;
        init();
    }

    public void show()
    {
        if(_dialog==null)
            _dialog=_build.create();
        if(!_dialog.isShowing())
            _dialog.show();
    }

    public void dismiss()
    {
        if(_dialog.isShowing())
        {
            _dialog.dismiss();
//            _dialog=null;
        }
    }

    protected void init()
    {
        if(_build==null)
            _build=new AlertDialog.Builder(_context);
    }

    protected Dialog createDialog()
    {
        return _dialog=_build.create();
    }

    public Dialog getDialog()
    {
        return _dialog;
    }
}
