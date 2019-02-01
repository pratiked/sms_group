package assignment.sms.group.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import assignment.sms.group.R;

public class SmsViewHolder extends RecyclerView.ViewHolder {

    private TextView txtAddress;
    private TextView txtBody;
    private TextView txtNew;

    public SmsViewHolder(@NonNull View itemView) {
        super(itemView);

        txtAddress = itemView.findViewById(R.id.txt_address);
        txtBody = itemView.findViewById(R.id.txt_body);
        txtNew = itemView.findViewById(R.id.txt_new);
    }

    public void setAddress(String address){
        txtAddress.setText(address);
    }

    public void setBody(String body){
        txtBody.setText(body);
    }

    public void setVisibilityNew(boolean isNew, int position){
        if (isNew && position == 1){
            txtNew.setVisibility(View.VISIBLE);
        } else {
            txtNew.setVisibility(View.GONE);
        }
    }
}
