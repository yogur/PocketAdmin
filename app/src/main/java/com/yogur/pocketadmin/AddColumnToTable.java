package com.yogur.pocketadmin;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abed on 1/31/16.
 */
public class AddColumnToTable extends Fragment implements View.OnClickListener{

    private String TableName = "";
    private View view;
    private TableLayout addColumnToTable;
    private Button button_save_add_column,button_cancel_add_column;
    private List<EditText> colNames;
    private List<EditText> typeLength;
    private List<Spinner> SQL_types;
    private List<Spinner> defaultValuesSpinner;
    private List<EditText> definedDefaultValue;
    private List<CheckBox> checkBoxesNull;
    private List<CheckBox> checkBoxesAutoIncrement;
    private ArrayAdapter<CharSequence> sqlTypesAdapter;
    private ArrayAdapter <CharSequence> defaultValuesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_add_column,container,false);
        addColumnToTable = (TableLayout) view.findViewById(R.id.addColumnToTable);

        button_save_add_column = (Button) view.findViewById(R.id.button_add_column_to_table);
        button_save_add_column.setOnClickListener(this);

        button_cancel_add_column = (Button) view.findViewById(R.id.button_cancel_add_column);
        button_cancel_add_column.setOnClickListener(this);

        colNames = new ArrayList<>();
        SQL_types = new ArrayList<>();
        typeLength = new ArrayList<>();
        definedDefaultValue = new ArrayList<>();
        defaultValuesSpinner = new ArrayList<>();
        checkBoxesNull = new ArrayList<>();
        checkBoxesAutoIncrement = new ArrayList<>();

        sqlTypesAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.SQL_types,android.R.layout.simple_spinner_item);
        sqlTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        defaultValuesAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.default_values,android.R.layout.simple_spinner_item);
        defaultValuesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Bundle extras = getArguments();

        if(extras != null){
            TableName = extras.getString("tableName");
            addRow(1);
        }

        return view;
    }

    protected void addRow(int number){

        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT,TableRow.LayoutParams.WRAP_CONTENT);

        for(int i = 0; i < number; i++){

            TableRow row = new TableRow(getActivity().getApplicationContext());
            row.setLayoutParams(layoutParams);

            //EditText of column name
            EditText editTextName = new EditText(getActivity().getApplicationContext());
            colNames.add(editTextName);

            //spinner of column type
            final Spinner type = new Spinner(getActivity().getApplicationContext());
            type.setAdapter(sqlTypesAdapter);
            SQL_types.add(type);

            //EditText of type length/value
            EditText editTextTypeLength = new EditText(getActivity().getApplicationContext());
            typeLength.add(editTextTypeLength);

            //EditText of defined default value
            final EditText editTextDefVal = new EditText(getActivity().getApplicationContext());
            editTextDefVal.setEnabled(false);
            definedDefaultValue.add(editTextDefVal);

            //define default spinner
            final Spinner defSpinner = new Spinner(getActivity().getApplicationContext());

            //checkboxes to define null values
            final CheckBox checkBoxNull = new CheckBox(getActivity().getApplicationContext());
            checkBoxNull.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //if checkbox null is unchecked and defaultSpinner is set to NULL change it to none
                    if(!buttonView.isChecked())
                        if(defSpinner.getSelectedItem().toString().equals("NULL"))
                            defSpinner.setSelection(0);

                }
            });
            checkBoxesNull.add(checkBoxNull);

            //spinner of default value
            defSpinner.setAdapter(defaultValuesAdapter);
            defSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position != 2) {
                        //disable field of specified default value
                        editTextDefVal.setEnabled(false);
                    } else {
                        //enable field of default value on user choice
                        editTextDefVal.setEnabled(true);
                    }

                    if (position == 1) //set checkbox of null selected if null is selected as default value
                        checkBoxNull.setChecked(true);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            defaultValuesSpinner.add(defSpinner);

            //checkboxes to specify whether the field uses autoincrement
            final CheckBox checkBoxAutoIncrement = new CheckBox(getActivity().getApplicationContext());
            checkBoxAutoIncrement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    String typeValue = type.getSelectedItem().toString();

                    if(buttonView.isChecked())
                        if(!(typeValue.equals("INT") || typeValue.equals("FLOAT")))
                        {
                            checkBoxAutoIncrement.setChecked(false);
                            Toast.makeText(getActivity().getApplicationContext(),"Type is not numeric!",Toast.LENGTH_LONG).show();
                        }
                }
            });
            checkBoxesAutoIncrement.add(checkBoxAutoIncrement);

            row.addView(editTextName);//add edittext column name to row
            row.addView(type);//add spinner of types to row
            row.addView(editTextTypeLength);//add edittext of type length/value to row
            row.addView(defSpinner);//add default spinner to row
            row.addView(editTextDefVal);//add edittext of specified default value to row
            row.addView(checkBoxNull);//add checkbox of null to row
            row.addView(checkBoxAutoIncrement);//add check box of auto_increment to row

            addColumnToTable.addView(row);//add row to tablelayout
        }
    }


    protected void addColumn(){

        if(validateFields() == false)
            return;

        final String createQuery = generateAddColumnQuery();

        final create_connection conn = ((MyApplication) getActivity().getApplication()).getConn();

        new Thread(new Runnable() {

            boolean success = true;
            String error;

            @Override
            public void run() {
                try {
                    conn.execQuery(conn.getConnection(),createQuery);
                } catch (SQLException e) {
                    e.printStackTrace();
                    success = false;
                    error = e.getMessage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                view.post(new Runnable() {
                    @Override
                    public void run() {
                        if (success){
                            gotoTableStructure();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),error,Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    protected boolean validateFields(){

        for(int i = 0; i < colNames.size(); i++) {

            //get column name then trim left and right spaces
            String columnName = colNames.get(i).getText().toString().trim();

            if (columnName.isEmpty()){
                colNames.get(i).setError("Column name can't be empty!");
                return false;
            }

            String sqlType = SQL_types.get(i).getSelectedItem().toString();
            String typLen = typeLength.get(i).getText().toString().trim();
            String defaultType = defaultValuesSpinner.get(i).getSelectedItem().toString();

            if((sqlType.equals("VARCHAR") || sqlType.equals("CHAR")) && typLen.isEmpty()){
                typeLength.get(i).setError("Selected SQL type requires a specified length");
                return false;
            }

            //if selected type is TEXT and a default value is specified, display an error
            if(sqlType.equals("TEXT") && defaultType.equals("As defined:")){
                definedDefaultValue.get(i).setError("TEXT can't have a default value!");
                return false;
            }

            if(checkBoxesAutoIncrement.get(i).isChecked()) {

                if(!defaultType.equals("None")){
                    Toast.makeText(getActivity().getApplicationContext(),"Auto number can't have a default value",Toast.LENGTH_LONG).show();
                    return false;
                }

                if(checkBoxesNull.get(i).isChecked()){
                    Toast.makeText(getActivity().getApplicationContext(),"Auto number can't be NULL",Toast.LENGTH_LONG).show();
                    return false;
                }
            }

        }

        return true;
    }

    protected String generateAddColumnQuery(){

        String query = "ALTER TABLE ";
        query += TableName + " ADD ";//append table name to query

            query += colNames.get(0).getText().toString() + " ";//add column name from edittext
            query += SQL_types.get(0).getSelectedItem().toString();//add column type from spinner

            String typLen = typeLength.get(0).getText().toString().trim();

            if(!typLen.isEmpty())
                query += "(" + typLen + ") ";

            if(checkBoxesNull.get(0).isChecked())
                query += " NULL ";
            else
                query += " NOT NULL ";

            if(checkBoxesAutoIncrement.get(0).isChecked())
                query += " PRIMARY KEY AUTO_INCREMENT ";

            String default_value = defaultValuesSpinner.get(0).getSelectedItem().toString();

            if(default_value.equals("NULL"))
                query += "DEFAULT NULL ";
            else
            if(default_value.equals("As defined:"))
                query += "DEFAULT " + "'" + definedDefaultValue.get(0).getText().toString() + "'";

        return query;
    }

    protected void gotoTableStructure(){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new tableStructFrag();
        Bundle data = new Bundle();
        data.putString("tableName", TableName);
        f.setArguments(data);
        transaction.replace(R.id.root_frame,f,"tableStructFrag");
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_add_column_to_table){
            addColumn();
        }

        if(v.getId() == R.id.button_cancel_add_column)
            gotoTableStructure();
    }
}
