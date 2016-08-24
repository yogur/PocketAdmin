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
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by abed on 1/24/16.
 */
public class tableStructFrag extends Fragment{

    private create_connection conn;
    private View tableStructView;
    private TableLayout table_layout_structure;
    private String tableName;
    private Button button_cancel,button_go_to_add_column;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        tableStructView = inflater.inflate(R.layout.fragment_table_structure,container,false);
        table_layout_structure = (TableLayout) tableStructView.findViewById(R.id.table_layout_structure);

        conn = ((MyApplication) getActivity().getApplication()).getConn();

        Bundle extras = getArguments();
        if(extras != null)
            tableName = extras.getString("tableName");

        button_cancel = (Button) tableStructView.findViewById(R.id.button_cancel_table_structure);
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoTableList();
            }
        });

        button_go_to_add_column = (Button) tableStructView.findViewById(R.id.button_go_to_add_column);
        button_go_to_add_column.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAddColumn();
            }
        });

        generateTableStructure(tableName);

        return tableStructView;
    }

    protected void gotoTableList(){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.root_frame, new tableList(), "tableListFragment");
        transaction.commit();
    }

    protected void goToAddColumn(){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new AddColumnToTable();
        Bundle data = new Bundle();
        data.putString("tableName", tableName);
        f.setArguments(data);
        transaction.replace(R.id.root_frame,f,"AddColumnToTable");
        transaction.commit();
    }

    protected void goToEditColumnDef(String columnName,String columnType,int columnSize,String columnNullStatus,String columnDefault,String AI){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new editColumnDefinition();
        Bundle data = new Bundle();
        data.putString("tableName", tableName);
        data.putString("columnName",columnName);
        data.putString("columnType",columnType);
        data.putString("columnSize", String.valueOf(columnSize));
        data.putString("columnNullStatus", columnNullStatus);
        data.putString("columnDefault",columnDefault);
        data.putString("AI",AI);
        f.setArguments(data);
        transaction.replace(R.id.root_frame,f,"editColumnDefinition");
        transaction.commit();
    }

    protected void generateTableStructure(String tname){

        final String tn = tname;
        final TableRow.LayoutParams tableRowLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

        new Thread(new Runnable() {

            List<String> columnNames;
            List<String> columnTypes;
            List<Integer> columnSizes;
            List<String> columnNullable;
            List<String> columnDefaultValue;
            List<String> columnIsAutoIncrement;
            List<String> primaryKeysList;
            List<String> uniqueColumnsList;
            boolean pkExists = false;

            @Override
            public void run() {

                try {
                    columnNames = conn.getColumnNames(conn.getConnection(), tn);
                    columnTypes = conn.getColumnTypes(conn.getConnection(), tn);
                    columnSizes = conn.getColumnSizes(conn.getConnection(), tn);
                    columnNullable = conn.getColumnNullable(conn.getConnection(), tn);
                    columnDefaultValue = conn.getColumnDefaultValues(conn.getConnection(), tn);
                    columnIsAutoIncrement = conn.getColumnIsAutoIncrement(conn.getConnection(), tn);
                    primaryKeysList = conn.getPrimarykeysList(conn.getConnection(), tn);
                    uniqueColumnsList = conn.getUniqueColumnsList(conn.getConnection(), tn);

                    pkExists = checkPKs(primaryKeysList);
                } catch (SQLException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


                tableStructView.post(new Runnable() {
                    @Override
                    public void run() {

                        //table head
                        TableRow tableHead = new TableRow(getActivity().getApplicationContext());
                        TextView headerColName = createTextView("Name");
                        tableHead.addView(headerColName);
                        TextView headerColType = createTextView("Type");
                        tableHead.addView(headerColType);
                        TextView headerNull = createTextView("Null");
                        tableHead.addView(headerNull);
                        TextView headerDefault = createTextView("Default");
                        tableHead.addView(headerDefault);
                        TextView headerExtra = createTextView("Extra");
                        tableHead.addView(headerExtra);
                        TextView headerAction = createTextView("Action");
                        //headerAction.setWidth(100);
                        tableHead.addView(headerAction);
                        table_layout_structure.addView(tableHead);

                        for(int i = 0; i < columnNames.size(); i++) {
                            TableRow row = new TableRow(getActivity().getApplicationContext());
                            row.setLayoutParams(tableRowLayoutParams);

                            //column name

                            final String columnName = columnNames.get(i);
                            TextView colName = createTextView(columnName);
                            row.addView(colName);

                            //column type and size
                            TextView colType = createTextView("");
                            int colSize = columnSizes.get(i);
                            String type = columnTypes.get(i);
                            //if column size is zero do not display it
                            if(colSize > 0)
                                type += "(" + colSize + ")";

                            colType.setText(type);
                            row.addView(colType);

                            //column null status
                            TextView colNullable = createTextView("");
                            String nullable = columnNullable.get(i);
                            if(nullable.equals("NULL"))
                                colNullable.setText("Yes");
                            else
                                colNullable.setText("No");
                            row.addView(colNullable);

                            //column default value
                            TextView colDefVal = createTextView("");
                            String defVal = columnDefaultValue.get(i);
                            if(defVal != null)
                                colDefVal.setText(defVal);
                            else
                                colDefVal.setText("None");
                            row.addView(colDefVal);

                            //column Extras
                            TextView colExtras = createTextView("");
                            String extras = "";
                            //check if is auto increment
                            String autonumber = columnIsAutoIncrement.get(i);
                            if(autonumber.equals("YES"))
                                extras += "AUTO_INCREMENT";
                            colExtras.setText(extras);
                            row.addView(colExtras);

                            //add button alter column
                            final String FinalcolType = columnTypes.get(i);
                            final int FinalcolSize = colSize;
                            final String FinalNullable = nullable;
                            final String FinaldefVal = defVal;
                            final String Finalautonumber = autonumber;
                            Button changeCol = new Button(getActivity().getApplicationContext());
                            changeCol.setText("Change");
                            changeCol.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    goToEditColumnDef(columnName,FinalcolType,FinalcolSize,FinalNullable,FinaldefVal,Finalautonumber);
                                }
                            });
                            row.addView(changeCol);

                            //add button drop column
                            Button drop = new Button(getActivity().getApplicationContext());
                            drop.setText("Drop");
                            final String sqlDrop = "ALTER TABLE " + tn + " DROP " + columnName;
                            drop.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    execSQL(sqlDrop, "Column dropped successfully");
                                }
                            });
                            row.addView(drop);

                            //add button primary key
                            Button primary = new Button(getActivity().getApplicationContext());
                            primary.setText("Primary");

                            //first drop primary key, to set a new one (if a primary key already exists)
                            String sqlPrimary = "";
                            if(pkExists)
                                sqlPrimary = "ALTER TABLE "+ tn +" DROP PRIMARY KEY;";//drop existing primary key

                            sqlPrimary += "ALTER TABLE " + tn + " ADD PRIMARY KEY (" + columnName + ")";

                            if(!primaryKeysList.contains(columnName)){
                                primary.setBackgroundColor(Color.YELLOW);
                                primary.setTextColor(Color.BLACK);
                                final String finalSqlPrimary = sqlPrimary;
                                primary.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        execSQL(finalSqlPrimary, "Column set primary successfully");
                                    }
                                });
                            } else {
                                primary.setEnabled(false);
                            }
                            row.addView(primary);

                            Button unique = new Button(getActivity().getApplicationContext());
                            unique.setText("Unique");

                            //if a column is not unique
                            if(!uniqueColumnsList.contains(columnName)){
                                unique.setBackgroundColor(Color.CYAN);
                                unique.setTextColor(Color.BLACK);
                                final String sqlUnique = "ALTER TABLE " + tn + " ADD UNIQUE (" + columnName + ")";
                                unique.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        execSQL(sqlUnique,"Column set unique successfully");
                                    }
                                });
                            } else {
                                unique.setEnabled(false);
                            }
                            row.addView(unique);

                            //add row to table layout
                            table_layout_structure.addView(row);
                        }
                    }
                });


            }
        }).start();
    }

    protected void refreshStructure(){

        table_layout_structure.removeAllViews();
        generateTableStructure(tableName);
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

    protected boolean checkPKs(List<String> listToCheck){

        boolean nonNullElemExist= false;
        for (String s: listToCheck) {
            if (s != null) {
                nonNullElemExist = true;
                break;
            }
        }

        return nonNullElemExist;
    }

    protected void execSQL(String sql,String successMsg){

        final String SqlStatement = sql;
        final String finalSuccessMsg = successMsg;

        new Thread(new Runnable() {
            @Override
            public void run() {

                String error = "";

                try {
                    conn.execQuery(conn.getConnection(),SqlStatement);
                } catch (SQLException e) {
                    e.printStackTrace();
                    error = e.getMessage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                final String finalError = error;
                tableStructView.post(new Runnable() {
                    @Override
                    public void run() {
                        if(finalError.equals("")) {
                            Toast.makeText(getActivity().getApplicationContext(), finalSuccessMsg, Toast.LENGTH_LONG).show();
                            refreshStructure();
                        }
                        else
                            Toast.makeText(getActivity().getApplicationContext(),finalError,Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).start();
    }

}
