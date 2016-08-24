package com.yogur.pocketadmin;

import android.graphics.Color;
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
 * Created by abed on 1/19/16.
 */
public class createTableFrag extends Fragment implements View.OnClickListener{

    private View view;
    private TableLayout createTableColumns;
    private Button button_save_created_table,button_add_column,button_cancel_created_table;
    private EditText input_table_name,input_number_columns;
    private List<EditText> colNames;
    private List<EditText> typeLength;
    private List<Spinner> SQL_types;
    private List<Spinner> defaultValuesSpinner;
    private List<EditText> definedDefaultValue;
    private List<CheckBox> checkBoxesNull;
    private List<CheckBox> checkBoxesAutoIncrement;
    private List<TableRow> rows;
    private List<Integer> indices;
    private ArrayAdapter<CharSequence> sqlTypesAdapter;
    private ArrayAdapter <CharSequence> defaultValuesAdapter;
    private int numRows = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_create_table,container,false);
        createTableColumns = (TableLayout) view.findViewById(R.id.createTableColumns);

        input_table_name = (EditText) view.findViewById(R.id.input_table_name);
        input_number_columns = (EditText) view.findViewById(R.id.input_number_columns);

        button_save_created_table = (Button) view.findViewById(R.id.button_save_created_table);
        button_save_created_table.setOnClickListener(this);

        button_cancel_created_table = (Button) view.findViewById(R.id.button_cancel_created_table);
        button_cancel_created_table.setOnClickListener(this);

        button_add_column = (Button) view.findViewById(R.id.button_add_column);
        button_add_column.setOnClickListener(this);

        colNames = new ArrayList<>();
        SQL_types = new ArrayList<>();
        typeLength = new ArrayList<>();
        definedDefaultValue = new ArrayList<>();
        defaultValuesSpinner = new ArrayList<>();
        checkBoxesNull = new ArrayList<>();
        checkBoxesAutoIncrement = new ArrayList<>();
        rows = new ArrayList<>();
        indices = new ArrayList<>();

        sqlTypesAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.SQL_types,android.R.layout.simple_spinner_item);
        sqlTypesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        defaultValuesAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),R.array.default_values,android.R.layout.simple_spinner_item);
        defaultValuesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Bundle extras = getArguments();

        if(extras != null){
            int numColumns = extras.getInt("numColumns");
            String newTableName = extras.getString("newTableName");
            input_table_name.setText(newTableName);
            addRow(numColumns);
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

            //button delete row
            final int temp = numRows;
            //System.out.println(getMaxIndex(indices) + 1);
            indices.add(getMaxIndex(indices) + 1);
            Button bDeleteRow = new Button(getActivity().getApplicationContext());
            bDeleteRow.setText("X");
            bDeleteRow.setId(temp);
            bDeleteRow.setBackgroundColor(Color.RED);
            bDeleteRow.setTextColor(Color.BLACK);
            bDeleteRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeRow(indices.get(temp));
                    //System.out.println(indices.get(temp));
                    indices.set(temp, -1);
                }
            });

            row.addView(editTextName);//add edittext column name to row
            row.addView(type);//add spinner of types to row
            row.addView(editTextTypeLength);//add edittext of type length/value to row
            row.addView(defSpinner);//add default spinner to row
            row.addView(editTextDefVal);//add edittext of specified default value to row
            row.addView(checkBoxNull);//add checkbox of null to row
            row.addView(checkBoxAutoIncrement);//add check box of auto_increment to row
            row.addView(bDeleteRow);//add button to remove current row

            numRows++;
            rows.add(row);//add row to list of rows
            createTableColumns.addView(row);//add row to tablelayout
        }
    }

    protected int getMaxIndex(List<Integer> list){

        if(list.isEmpty())
            return -1;

        int max = -1;

        for(int i = 0; i < list.size(); i++){
            int temp = list.get(i);

            if(temp > max)
                max = temp;
        }

        return max;
    }

    protected void removeRow(int num){

        //remove all fields from lists
        colNames.remove(num);
        typeLength.remove(num);
        SQL_types.remove(num);
        defaultValuesSpinner.remove(num);
        definedDefaultValue.remove(num);
        checkBoxesNull.remove(num);
        checkBoxesAutoIncrement.remove(num);

        //remove all children of row
        rows.get(num).removeAllViews();
        //remove row from tablelayout
        createTableColumns.removeView(rows.get(num));
        //remove row from rows list
        rows.remove(num);

        //update indices list
        for(int i = 0; i < indices.size(); i++){
            if(indices.get(i) > num){
                int temp = indices.get(i);
                indices.set(i,temp - 1);
            }
        }
    }

    protected void createTable(){

        if(validateFields() == false)
            return;

        final String createQuery = generateCreateQuery();

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
                            FragmentTransaction transaction = getFragmentManager()
                                    .beginTransaction();
                            transaction.replace(R.id.root_frame, new tableList(),"tableListFragment");
                            transaction.commit();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),error,Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    protected boolean validateFields(){

        //check for duplicate columns names
        for(int i = 0; i < colNames.size(); i++){
            String temp = colNames.get(i).getText().toString().trim();

            //column name not entered, error will be caught later on
            if(temp.isEmpty())
                continue;

            for(int j = i + 1; j < colNames.size(); j++){
                String tempTwo = colNames.get(j).getText().toString().trim();

                //column name not entered, error will be caught later on
                if(tempTwo.isEmpty())
                    continue;

                if(temp.equals(tempTwo)){
                    Toast.makeText(getActivity().getApplicationContext(),"Duplicate column name " + temp,Toast.LENGTH_LONG).show();
                    return false;
                }
            }
        }

        int countAutoIncrementChecks = 0;

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

                countAutoIncrementChecks++;
            }

        }

        if(countAutoIncrementChecks > 1){
            Toast.makeText(getActivity().getApplicationContext(),"There can be only one auto column!",Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    protected String generateCreateQuery(){

        String query = "CREATE TABLE ";
        String tableName = input_table_name.getText().toString();
        query += tableName + " (";//append table name to query

        for(int i = 0; i < colNames.size(); i++){

            query += colNames.get(i).getText().toString() + " ";//add column name from edittext
            query += SQL_types.get(i).getSelectedItem().toString();//add column type from spinner

            String typLen = typeLength.get(i).getText().toString().trim();

            if(!typLen.isEmpty())
                query += "(" + typLen + ") ";

            if(checkBoxesNull.get(i).isChecked())
                query += " NULL ";
            else
                query += " NOT NULL ";

            if(checkBoxesAutoIncrement.get(i).isChecked())
                query += " PRIMARY KEY AUTO_INCREMENT ";

            String default_value = defaultValuesSpinner.get(i).getSelectedItem().toString();

            if(default_value.equals("NULL"))
                query += "DEFAULT NULL ";
            else
                if(default_value.equals("As defined:"))
                    query += "DEFAULT " + "'" + definedDefaultValue.get(i).getText().toString() + "'";

            if(i != colNames.size() - 1)
                query += ",";

        }

        query += ")";

        return query;
    }

    protected void gotoTableList(){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame, new tableList(), "tableListFragment");
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button_save_created_table){
            createTable();
        }

        if(v.getId() == R.id.button_cancel_created_table)
            gotoTableList();

        if(v.getId() == R.id.button_add_column){
            int numColumns = Integer.parseInt(input_number_columns.getText().toString());
            addRow(numColumns);
        }
    }
}
