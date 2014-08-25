package com.uninet.wirelessstore;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;






import java.util.ArrayList;
import java.util.List;


public class IconifiedTextListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private List<IconifiedText> mItems = new ArrayList<IconifiedText>();
	private int  selectItem=-1;
	
	public IconifiedTextListAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
    
	public  void setSelectItem(int selectItem) {  
        this.selectItem = selectItem;  
    }
    
	public void addItem(IconifiedText it) {
		mItems.add(it);
	}
	
	private class ViewHolder {
		private ImageView mIcon;
		private TextView mText;
		private TextView mInfo;
	}
	
	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		 return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public void setListItems(List<IconifiedText> lit) {
		mItems = lit;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewholder;
      IconifiedText item = mItems.get(position);
      if (convertView == null) {
          convertView = mInflater.inflate(R.layout.list_item, null);
          viewholder = new ViewHolder();

          viewholder.mIcon = (ImageView) convertView.findViewById(R.id.ItemImage);
          viewholder.mText = (TextView) convertView.findViewById(R.id.ItemTitle);
          viewholder.mInfo = (TextView) convertView.findViewById(R.id.Iteminfo);

          convertView.setTag(viewholder);
          android.util.Log.d("abc","aaaa ggg");
      } else {
    	  viewholder = (ViewHolder) convertView.getTag();
    	  android.util.Log.d("abc","aaaa fff");
      }
      android.util.Log.d("abc","item = " + item.getText());
      viewholder.mText.setText(item.getText());
      viewholder.mInfo.setText(item.getInfo());
      viewholder.mIcon.setImageDrawable(item.getIcon());

      if (position == selectItem) {  
          convertView.setBackgroundColor(Color.RED);
      } else {  
          convertView.setBackgroundColor(Color.TRANSPARENT);  
      }
      
      return convertView;
	}
}