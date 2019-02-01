package assignment.sms.group.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import assignment.sms.group.MainActivity.SmsItem;
import assignment.sms.group.MainActivity.HeaderItem;
import assignment.sms.group.MainActivity.ListItem;
import assignment.sms.group.R;
import assignment.sms.group.holders.HeaderViewHolder;
import assignment.sms.group.holders.SmsViewHolder;

public class GroupedSmsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ListItem> mItemList;
    private boolean mNewSms;

    public GroupedSmsAdapter(List<ListItem> itemList, boolean newSms) {
        this.mItemList = itemList;
        this.mNewSms = newSms;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View itemView;
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType){
            case ListItem.TYPE_HEADER:
                itemView = layoutInflater.inflate(R.layout.item_header, viewGroup, false);
                return new HeaderViewHolder(itemView);
            default:
            case ListItem.TYPE_SMS:
                itemView = layoutInflater.inflate(R.layout.item_sms, viewGroup, false);
                return new SmsViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

        switch (viewHolder.getItemViewType()){
            case ListItem.TYPE_HEADER:

                HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
                HeaderItem headerItem = (HeaderItem) mItemList.get(i);

                headerViewHolder.setHeader(headerItem.getHeader());

                break;
            case ListItem.TYPE_SMS:

                SmsViewHolder smsViewHolder = (SmsViewHolder) viewHolder;
                SmsItem smsItem = (SmsItem) mItemList.get(i);

                smsViewHolder.setAddress(smsItem.getMySms().getAddress());
                smsViewHolder.setBody(smsItem.getMySms().getBody());

                smsViewHolder.setVisibilityNew(mNewSms, i);

                break;
        }
    }

    public void updateNew(boolean isNew){
        this.mNewSms = isNew;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position).getType();
    }
}
