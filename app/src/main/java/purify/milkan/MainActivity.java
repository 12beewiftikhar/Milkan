package purify.milkan;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends Activity implements View.OnClickListener {

    ViewFlipper flipper;
    LayoutInflater inflater;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    ArrayList<String> farmers;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        context=this.getApplicationContext();
        sharedPref= this.getPreferences(Context.MODE_PRIVATE);
        editor=sharedPref.edit();
        if(!sharedPref.contains("farmers")) {
            farmers=new ArrayList<String>();
            saveFarmers();
        }
        else{
            getFarmers();
        }
        inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //display the logo during 2 secondes,
        new CountDownTimer(2000,1000){
            @Override
            public void onTick(long millisUntilFinished){}

            @Override
            public void onFinish(){
                //set the new Content of your activity
                setContentView(R.layout.activity_main);
                flipper=(ViewFlipper)findViewById(R.id.ViewFlipper);
                View view = inflater.inflate(R.layout.getid, null);
                flipper.addView(view);
                view = inflater.inflate(R.layout.farmer, null);
                flipper.addView(view);
                view = inflater.inflate(R.layout.list_paramters, null);
                flipper.addView(view);

            }
        }.start();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.Go) {
            //hide keyboard
            hideKeyboard();
            EditText name,id;
            id=(EditText) findViewById(R.id.GetID);
            name=(EditText) findViewById(R.id.GetName);
            if(name.getText().toString().equals("")||id.getText().toString().equals("")) {
                Toast.makeText(this,"Enter both fields ",Toast.LENGTH_SHORT).show();
                return;
            }
            updateFarmerView();

            flipper.setDisplayedChild(1);
        }
        else if(v.getId()==R.id.newFarmer){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);

            alert.setTitle("Add Farmer");
            alert.setMessage("Enter name of farmer");

            // Set an EditText view to get user input
            final EditText input = new EditText(this);
            alert.setView(input);

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(input.getText()==null || input.getText().toString().compareTo("")==0) {
                        Toast.makeText(context, "Farmer not saved. Enter name again", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    saveFarmer(input.getText().toString());
                    updateFarmerView();
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });

            alert.show();
        }
        else if(v.getId()==R.id.list_item_farmer_name) {
            TextView v1 =(TextView)v;
            Toast.makeText(this,v1.getText()+"",Toast.LENGTH_SHORT).show();
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            RadioGroup group = (RadioGroup) findViewById(R.id.Accept_Reject);
            group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    Button send = (Button) findViewById(R.id.send);
                    send.setVisibility(View.VISIBLE);
                    if (R.id.Reject == checkedId) {
                        TextView temp = (TextView) findViewById(R.id.Quantity);
                        temp.setVisibility(View.GONE);
                        Spinner spin = (Spinner) findViewById(R.id.Units);
                        spin.setVisibility(View.GONE);
                    } else if (R.id.Accept == checkedId) {
                        TextView temp = (TextView) findViewById(R.id.Quantity);
                        temp.setVisibility(View.VISIBLE);
                        Spinner spin = (Spinner) findViewById(R.id.Units);
                        spin.setVisibility(View.VISIBLE);
                    }

                }
            });
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                int REQUEST_ENABLE_BT = 1;
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            flipper.setDisplayedChild(2);
            return;
        }

    }
    public  void hideKeyboard(){
        View view =getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public void updateFarmerView(){
        LinearLayout farmerLayout=(LinearLayout)findViewById(R.id.farmerLayout);
        farmerLayout.removeAllViews();
        if (farmers.size()==0){
            TextView noFarmers= new TextView(this);
            noFarmers.setText("No farmers to show");
            farmerLayout.addView(noFarmers);
        }
        for(int i=0;i<farmers.size();i++) {
            View list_item = inflater.inflate(R.layout.farmer_list_item, null);
            TextView farmerName=(TextView)list_item.findViewById(R.id.list_item_farmer_name);
            farmerName.setText(farmers.get(i));
            farmerLayout.addView(list_item);
        }
    }
    public boolean saveFarmer(String farmer){
        int size = sharedPref.getInt("farmers", 0);
        size++;
        editor = sharedPref.edit();
        editor.putInt("farmers", size);
        editor.putString("farmer_" + size, farmer);
        farmers.add(farmer);
        return editor.commit();
    }
    public boolean saveFarmers(){
        editor = sharedPref.edit();
        editor.putInt("farmers", farmers.size());

        for(int i=0;i<farmers.size();i++)
        {
            editor.remove("farmer_" + i);
            editor.putString("farmer_" + i, farmers.get(i));
        }

        return editor.commit();
    }
    public void getFarmers(){
        int size = sharedPref.getInt("farmers", 0);
        farmers=new ArrayList<String>();
        for(int i=1;i<=size;i++)
        {
            farmers.add(sharedPref.getString("farmer_" + i, null));
        }

    }
}
