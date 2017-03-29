package com.androidtech.around.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidtech.around.Activities.ConversionActivity;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Widgets.CircleTransformation;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.androidtech.around.Activities.ConversionActivity.USER_EXTRA;

public class InboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String TAG = "InboxAdapter";
    private Context context;
    private List<User> userItems;

    public InboxAdapter(Context context , List<User> userItems) {
        this.context = context;
        this.userItems = userItems;
    }

    public List<User> getUserItems() {
        return userItems;
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.inbox_user_image)
        ImageView userImage;

        @BindView(R.id.inbox_full_name)
        TextView userFullName;

        @BindView(R.id.last_message_txt)
        TextView lastMessageTxt;

        @BindView(R.id.last_message_time)
        TextView lastMessageTime;

        @BindView(R.id.inbox_item)
        RelativeLayout inboxItem;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.inbox_recycler_item,parent, false);
        RecyclerView.ViewHolder holder = null;
            holder = new CardViewHolder(view);
            final CardViewHolder cardHolder = (CardViewHolder)holder;

            //set click listener on card profile
            cardHolder.inboxItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    User user =userItems.get(cardHolder.getAdapterPosition());
                    Parcelable wrapped = Parcels.wrap(user);
                    Intent intent= new Intent(context, ConversionActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(USER_EXTRA, wrapped);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });

        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        // Just as an example, return 0 or 2 depending on position
        // Note that unlike in ListView adapters, types don't have to be contiguous
        return 1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final CardViewHolder cardViewHolder = (CardViewHolder) holder;

        User user = userItems.get(cardViewHolder.getAdapterPosition());

            if(user.getProfile_picture_url()!=null)
                Picasso.with(context).load(user.getProfile_picture_url()).transform(new CircleTransformation()).into(cardViewHolder.userImage);
        else
                cardViewHolder.userImage.setImageResource(R.drawable.profile_image);

            if(user.getFull_name()!=null)
                cardViewHolder.userFullName.setText(user.getFull_name());


        if(user.getLast_message()!=null){

            cardViewHolder.lastMessageTxt.setText(user.getLast_message());

            if(!user.isLast_message_seen()
                    && !user.getLast_message_sender().equals(FirebaseUtil.getCurrentUserId())){//message not seen
                cardViewHolder.lastMessageTxt.setTypeface(Typeface.DEFAULT_BOLD);
                cardViewHolder.lastMessageTxt.setTextSize(16);
                cardViewHolder.userFullName.setTypeface(Typeface.DEFAULT_BOLD);
                cardViewHolder.userFullName.setTextSize(18);

                cardViewHolder.lastMessageTime.setTypeface(Typeface.DEFAULT_BOLD);
            }else{
                cardViewHolder.lastMessageTxt.setTypeface(Typeface.DEFAULT);
                cardViewHolder.lastMessageTxt.setTextSize(14);
                cardViewHolder.userFullName.setTypeface(Typeface.DEFAULT);
                cardViewHolder.userFullName.setTextSize(16);

                cardViewHolder.lastMessageTime.setTypeface(Typeface.DEFAULT);
            }

            Date messageTime = new Date(user.getLast_message_timestamp());
            String messageTime_String = (String) DateUtils.getRelativeDateTimeString(context, messageTime.getTime(), DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.WEEK_IN_MILLIS, 0);
            //substring time string
            if(messageTime_String.contains(",")){
                String minutes = messageTime_String.substring(0, messageTime_String.indexOf(',')+1);
                messageTime_String = messageTime_String.replace(minutes,"");
            }

            cardViewHolder.lastMessageTime.setText(messageTime_String);

        }else{
            cardViewHolder.lastMessageTime.setVisibility(View.GONE);
            cardViewHolder.lastMessageTxt.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
            return userItems.size();
    }

    public void addAll(List<User> itemList){
        userItems.clear();
        userItems.addAll(itemList);
        notifyDataSetChanged();
    }

    public void addUser(User user){
        userItems.add(user);
        notifyDataSetChanged();
    }
}