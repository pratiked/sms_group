package assignment.sms.group.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import assignment.sms.group.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    private TextView txtHeader;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);

        txtHeader = itemView.findViewById(R.id.txt_header);
    }

    public void setHeader(String header){
        txtHeader.setText(header);
    }

}
