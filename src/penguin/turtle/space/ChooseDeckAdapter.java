package penguin.turtle.space;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

public class ChooseDeckAdapter extends ArrayAdapter<Deck>{

	 private Context context;
	 private TextView name;
	 private CheckBox isChecked;
	 private Deck deck;
	 
	 private ArrayList<Deck> decksList = new ArrayList<Deck>(); 
	
	 public ChooseDeckAdapter(Context context, int textViewResourceId, ArrayList<Deck> decksList) {
			super(context, textViewResourceId, decksList);
			this.context = context;
			this.decksList = decksList;
		}
	 
	 public int getCount() {
	        return this.decksList.size();
	    }
	 
	 public Deck getItem(int index) {
	        return this.decksList.get(index);
	    }
	 
	 public View getView(int position, View convertView, ViewGroup parent) {
	        View row = convertView;
	        if (row == null) {
	            LayoutInflater inflater = (LayoutInflater) this.getContext()
	                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            row = inflater.inflate(R.layout.deck_list, parent, false);
	        }
	 
	       //Get the object
	        deck = getItem(position);
	        
	        // get the TextView id
	        name = (TextView) row.findViewById(R.id.deckname);
	        
	        // to set the string information for each build
	        name.setText(deck.getName());	
	        
	        //Checkbox
	        isChecked = (CheckBox) row.findViewById(R.id.checkBox);
	        isChecked.setOnCheckedChangeListener(new OnCheckedChangeListener()
	        {
	            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	            {
	                if ( isChecked )
	                {  deck.setChecked(true);  }
	                else 
	                {   deck.setChecked(false);  }
	            }
	        });
	        return row;
	    }
	 
}
