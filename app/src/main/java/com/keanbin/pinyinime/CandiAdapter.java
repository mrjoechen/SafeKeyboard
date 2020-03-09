package com.keanbin.pinyinime;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.safe.keyboard.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenqiao on 2020/3/9.
 * e-mail : mrjctech@gmail.com
 */
public class CandiAdapter extends RecyclerView.Adapter<CandiAdapter.MyViewHolder> {

    private List<String> data ;

    public CandiAdapter() {
        data = new ArrayList<>();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_candidate,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.tv_candi.setText(position + "."+data.get(position));
        holder.tv_candi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void addData(List<String> stringList, boolean init){
        if (data != null && stringList != null){

            if (init){
                data.clear();
                data.addAll(stringList);
                notifyDataSetChanged();
            }else {
                data.addAll(stringList);
                notifyDataSetChanged();
            }

        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void removeData(){
        data.clear();
        notifyDataSetChanged();
    }


    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
    }

    static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView tv_candi;

        MyViewHolder(View itemView) {
            super(itemView);
            tv_candi = itemView.findViewById(R.id.tv_candi);
        }
    }
}
