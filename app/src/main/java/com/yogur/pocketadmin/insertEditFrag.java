package com.yogur.pocketadmin;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abed on 1/26/16.
 */
public class insertEditFrag extends Fragment implements View.OnClickListener{

    private View insertEditFragView;
    private TableLayout tableLayoutInsertEdit;
    private create_connection conn;
    private String pkName,pkValue,tableName;
    private boolean isNew;
    private Button bCancel,bSave;
    private List<String> columnNames,isNullable,autoIncrementStatus;
    private List<EditText> listValues;
    private List<CheckBox> checkBoxListNull;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        insertEditFragView = inflater.inflate(R.layout.fragment_insert_edit,container,false);

        tableLayoutInsertEdit = (TableLayout) insertEditFragView.findViewById(R.id.table_layout_insert_edit);
        bCancel = (Button) insertEditFragView.findViewById(R.id.button_cancel_insert_edit);
        bCancel.setOnClickListener(this);
        bSave = (Button) insertEditFragView.findViewById(R.id.button_save_insert_edit);
        bSave.setOnClickListener(this);

        listValues = new ArrayList<>();
        checkBoxListNull = new ArrayList<>();
        autoIncrementStatus = new ArrayList<>();

        conn = ((MyApplication) getActivity().getApplication()).getConn();

        //get pk name and value from extras
        Bundle extras = getArguments();
        if(extras != null){
            pkName = extras.getString("pkName");
            pkValue = extras.getString("pkValue");
            tableName = extras.getString("tableName");
            isNew = extras.getBoolean("isNew");
        }

        populateViews();

        return insertEditFragView;
    }

    protected void populateViews(){

        new Thread(new Runnable() {

            List<String> colTypeSize;
            List<String> defaultValues;
            List<String> rowData;

            @Override
            public void run() {

                try {
                    columnNames = conn.getColumnNames(conn.getConnection(), tableName);
                    colTypeSize = conn.getColumnTypeAndSize(conn.getConnection(), tableName);
                    isNullable = conn.getColumnNullable(conn.getConnection(), tableName);
                    autoIncrementStatus = conn.getColumnIsAutoIncrement(conn.getConnection(),tableName);

                    //if the row in newly inserted, get default values to autofill textviews
                    if(isNew == true)
                        defaultValues = conn.getColumnDefaultValues(conn.getConnection(),tableName);
                    else
                        rowData = conn.getSingleRow(conn.getConnection(),tableName,pkName,pkValue);

                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                insertEditFragView.post(new Runnable() {
                    @Override
                    public void run() {

                        for(int i = 0; i < columnNames.size(); i++){

                            TableRow row = new TableRow(getActivity().getApplicationContext());

                            //column name
                            TextView Name = createTextView("");
                            Name.setText(columnNames.get(i));
                            row.addView(Name);

                            //column type and size
                            TextView TypeSize = createTextView("");
                            TypeSize.setText(colTypeSize.get(i));
                            row.addView(TypeSize);

                            final EditText value = new EditText(getActivity().getApplicationContext());;
                            //null or not null
                            CheckBox nullStatus = new CheckBox(getActivity().getApplicationContext());
                            if(isNullable.get(i).equals("NOT NULL")) {
                                nullStatus.setEnabled(false);
                                nullStatus.setBackgroundColor(Color.RED);
                            }
                            else {

                                if(isNew == true || rowData.get(i) == null)
                                    nullStatus.setChecked(true);

                                nullStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if(buttonView.isChecked())
                                            value.setText("");
                                    }
                                });
                            }
                            checkBoxListNull.add(nullStatus);
                            row.addView(nullStatus);

                            //value or default value
                            if(isNew == true) {
                                String temp = defaultValues.get(i);
                                if(temp != null)
                                    value.setText(defaultValues.get(i));
                            }
                            else
                                value.setText(rowData.get(i));
                            //add edit text to list, then to row
                            listValues.add(value);
                            row.addView(value);

                            //add row to table layout
                            tableLayoutInsertEdit.addView(row);
                        }
                    }
                });
            }
        }).start();
    }

    protected void gotoTableList(){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame,new tableList(),"tableListFragment");
        transaction.commit();
    }

    protected TextView createTextView(String value){

        TextView tv = new TextView(getActivity().getApplicationContext());
        tv.setText(value);
        tv.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.cell_shape));
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(5, 5, 5, 5);
        tv.setTextColor(Color.BLACK);

        return tv;
    }

    protected void executeOperation(){

        final String sql = parseFields();

        new Thread(new Runnable() {
            @Override
            public void run() {

                String error = "";

                try {
                    conn.execQuery(conn.getConnection(),sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                    error = e.getMessage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                final String finalError = error;
                insertEditFragView.post(new Runnable() {
                    @Override
                    public void run() {
                        if(finalError.equals(""))
                            Toast.makeText(getActivity().getApplicationContext(), "Column inserted/updated successfully", Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity().getApplicationContext(),finalError,Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

    protected String parseFields(){

        String sql = "";

        //insert query
        if(isNew == true){
            sql += "INSERT INTO " + tableName + " (";

            String cNames = "",cValue = ") VALUES (";

            for(int i = 0; i < columnNames.size();i++){

                //append column names to string of column names
                cNames += columnNames.get(i);

                boolean isCheckedNull = checkBoxListNull.get(i).isChecked();
                String value = listValues.get(i).getText().toString().trim();
                String nullStatus = isNullable.get(i);

                if(autoIncrementStatus.get(i).equals("YES") && value.isEmpty())//if the column is autonumber and the value field is empty
                    cValue += "NULL"; //append value null
                else if(nullStatus.equals("NOT NULL"))//can't be null
                    cValue += "'" + value + "'";
                else if(isCheckedNull == true && value.isEmpty())//if null is checked and the edittext is empty
                    cValue += "NULL"; //append value null
                else //if edittext contains a value, append it
                    cValue += "'" + value + "'";

                if(i < columnNames.size() - 1) {
                    cNames += ",";
                    cValue += ",";
                }
            }

            sql += cNames + cValue + ")";
        }
        else //update query
        {
            sql += "UPDATE " + tableName + " SET ";

            for(int i = 0; i < columnNames.size();i++){

                //column name
                String cn = columnNames.get(i);

                boolean isCheckedNull = checkBoxListNull.get(i).isChecked();
                String value = listValues.get(i).getText().toString().trim();
                String nullStatus = isNullable.get(i);

                if(autoIncrementStatus.get(i).equals("YES") && value.isEmpty())//if the column is autonumber and the value field is empty
                    sql += cn + "=NULL";
                else if(nullStatus.equals("NOT NULL"))//can't be null
                    sql += cn + "='" + value + "'";
                else if(isCheckedNull == true && value.isEmpty())//if null is checked and the edittext is empty
                    sql += cn + "=NULL";
                else //if edittext contains a value, append it
                    sql += cn + "='" + value + "'";

                if(i < columnNames.size() - 1)
                    sql += ",";
            }

            sql += " WHERE " + pkName + " = '" + pkValue + "'";
        }

        return sql;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.button_cancel_insert_edit:
                gotoTableList();
                break;

            case R.id.button_save_insert_edit:
                    executeOperation();
                break;
        }
    }
}
