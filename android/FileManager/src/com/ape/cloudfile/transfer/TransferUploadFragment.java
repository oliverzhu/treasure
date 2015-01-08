package com.ape.cloudfile.transfer;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.ape.cloudfile.transfer.TransferService.TransferType;
import com.ape.filemanager.R;
import com.cloud.client.file.MissionObject;

public class TransferUploadFragment extends TransferFragment
{

    @Override
    protected String getTransferringPrompt()
    {
        return getString(R.string.uploading_list, mTransferringList.size());
    }

    @Override
    protected String getTransferredPrompt()
    {
        return getString(R.string.uploaded_list, mTransferredList.size());
    }

    @Override
    public TransferType getTransferType()
    {
        return TransferType.upload;
    }

    @Override
    public void deleteFiles()
    {
        final ArrayList<MissionObject> deleteList = new ArrayList<MissionObject>();
        deleteList.addAll(getCheckedList());

        Dialog dialog = new AlertDialog.Builder(mActivity)
            .setTitle(R.string.delete_upload_record)
            .setMessage(R.string.operation_delete_confirm_message)
            .setPositiveButton(R.string.yes, new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    mTransferService.deleteTransferList(deleteList, false,
                            getTransferType(), new DeleteListener());
                }
            })
            .setNegativeButton(R.string.no, new OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    new DeleteListener().onTaskResult(0);
                }
            }).create();
        dialog.show();
    }

}
