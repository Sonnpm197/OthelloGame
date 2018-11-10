package com.example.son.othellogame.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.son.othellogame.R;
import com.example.son.othellogame.entities.User;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.son.othellogame.entities.User.Status.OFFLINE;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context context;
    private List<User> users;
    private InviteInterface inviteInterface;

    public interface InviteInterface {
        void inviteFriend(String friendId, String friendStatus);
    }

    public UserAdapter(InviteInterface inviteInterface, Context context, List<User> users) {
        this.context = context;
        this.users = users;
        this.inviteInterface = inviteInterface;
    }

    @NonNull
    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, int position) {
        User user = users.get(position);
        holder.userName.setText(user.getUserName());
        holder.userId.setText(user.getId()); // this field is friend Id used to support inviting
        holder.setUserStatus(user.getStatus());

        // set online symbol next to user name
        if (user.getStatus().equals(OFFLINE.getValue())) {
            holder.img_off.setVisibility(View.VISIBLE);
            holder.img_on.setVisibility(View.GONE);
        } else {
            holder.img_off.setVisibility(View.GONE);
            holder.img_on.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView userName;
        TextView userId;
        CircleImageView img_on;
        CircleImageView img_off;
        Button btnInvite;
        String userStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            userName = (TextView) itemView.findViewById(R.id.userName);
            userId = (TextView) itemView.findViewById(R.id.userId);
            img_on = (CircleImageView) itemView.findViewById(R.id.img_on);
            img_off = (CircleImageView) itemView.findViewById(R.id.img_off);
            btnInvite = (Button) itemView.findViewById(R.id.btnInvite);

            btnInvite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    inviteInterface.inviteFriend(userId.getText().toString(), userStatus);
                }
            });
        }

        /**
         * Set this string to handle handle case when fiend is offline
         * @param userStatus
         */
        public void setUserStatus(String userStatus) {
            this.userStatus = userStatus;
        }
    }
}
