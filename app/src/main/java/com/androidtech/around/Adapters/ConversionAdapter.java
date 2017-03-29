package com.androidtech.around.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidtech.around.Models.Message;
import com.androidtech.around.Models.User;
import com.androidtech.around.R;
import com.androidtech.around.Utils.FirebaseUtil;
import com.androidtech.around.Utils.SharedPrefUtilities;
import com.androidtech.around.Widgets.CircleTransformation;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConversionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public String TAG = "ConversionAdapter";
    private Context context;
    private List<Message> messageItems;
    private User chattingUser;
    private SharedPrefUtilities mSharedPrefUtilities;

    public ConversionAdapter(Context context , List<Message> messageItems , User chattingUser) {
        this.context = context;
        this.messageItems = messageItems;
        this.chattingUser = chattingUser;
        //get instance from shared pref utility
        mSharedPrefUtilities = new SharedPrefUtilities(context);
    }

    class CardViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.message_user_image)
        ImageView userImage;

        @BindView(R.id.conv_message)
        TextView messageTxt;

        @BindView(R.id.conv_time)
        TextView messageTimeTxt;

        public CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if(viewType==1)
        view = LayoutInflater.from(context).inflate(R.layout.message_recycler_item_right,parent, false);
        else
            view = LayoutInflater.from(context).inflate(R.layout.message_recycler_item_left,parent, false);

        RecyclerView.ViewHolder holder = null;
            holder = new CardViewHolder(view);
            final CardViewHolder cardHolder = (CardViewHolder)holder;
        return holder;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageItems.get(position);
        if(message.getSenderUid().equals(FirebaseUtil.getCurrentUserId()))//check if i' sender or not
        return 1;
        else
            return 2;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final CardViewHolder cardViewHolder = (CardViewHolder) holder;

        Message message = messageItems.get(cardViewHolder.getAdapterPosition());

        String userImage ;
        if(message.getSenderUid().equals(FirebaseUtil.getCurrentUserId())){//iam sender show my data
            userImage = mSharedPrefUtilities.getUserImageFull();
        }else{//show image of another user
            userImage = chattingUser.getProfile_picture_url();
        }

        if(userImage!=null)
            Picasso.with(context).load(userImage).transform(new CircleTransformation()).into(cardViewHolder.userImage);
        else
            cardViewHolder.userImage.setImageResource(R.drawable.profile_image);

        cardViewHolder.messageTxt.setText(message.getMessage());

        Date messageTime = new Date(message.getTimestamp());
        String messageTime_String = (String) DateUtils.getRelativeDateTimeString(context, messageTime.getTime(), DateUtils.MINUTE_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);

        cardViewHolder.messageTimeTxt.setText(messageTime_String);
    }

    @Override
    public int getItemCount() {
            return messageItems.size();
    }

    public void addAll(List<Message> itemList){
        messageItems.clear();
        messageItems.addAll(itemList);
        notifyDataSetChanged();
    }

    public void addItem(Message message){
        messageItems.add(message);
       // notifyItemInserted(messageItems.size() - 1);
        notifyDataSetChanged();
    }

}