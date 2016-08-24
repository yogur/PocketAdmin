package com.yogur.pocketadmin;

import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by abed on 12/24/15.
 */
public class sqlFragment extends Fragment implements View.OnClickListener{

    private View sqlView;
    private TextView textView_sql;
    private Button exec;
    private RadioGroup radioGroup_queryType;
    private RadioButton radioButtonSelect;
    private EditText input_query;
    private create_connection conn;
    private TableLayout result_table;
    private ResultSet rs;

    public void setTextView_sql(String dbName){
        textView_sql.setText("Run SQL query/queries on database: " + dbName);
    }

    public void setInput_query(String q){
        input_query.setText(q);
    }

    public void setRadioButtonSelectChecked(){
        radioButtonSelect.setChecked(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sqlView = inflater.inflate(R.layout.fragment_sql,container,false);

        textView_sql = (TextView) sqlView.findViewById(R.id.textView_query);
        radioGroup_queryType = (RadioGroup) sqlView.findViewById(R.id.radioGroup_query_type);
        radioButtonSelect = (RadioButton) sqlView.findViewById(R.id.radioButton_select);
        input_query = (EditText) sqlView.findViewById(R.id.input_query);
        result_table = (TableLayout) sqlView.findViewById(R.id.result_table);

        exec = (Button) sqlView.findViewById(R.id.button_exec_sql);
        exec.setOnClickListener(this);


        String dbName = ((MyApplication) getActivity().getApplication()).getDbName();
        String hostName = ((MyApplication) getActivity().getApplication()).getHostName();

        if(dbName.equals("")){
            textView_sql.setText("Run SQL query/queries on server: " + hostName);
        } else {
            textView_sql.setText("Run SQL query/queries on database: " + dbName);
        }

        conn = ((MyApplication) getActivity().getApplication()).getConn();

        return sqlView;
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.button_exec_sql) {
            //checks if select or insert/ddl is selected and executes query
            String query = input_query.getText().toString();
            execSQL(query);
        }
    }

    protected void execSQL(String Query){

        final String query = Query;

        //select query
        if(radioGroup_queryType.getCheckedRadioButtonId() == R.id.radioButton_select){

            new Thread(new Runnable() {

                boolean success = true;
                String error;
                ResultSetMetaData rsMetaData;
                String uniqueColumnName;

                @Override
                public void run() {

                    boolean db_empty = false;
                    boolean noUnique = false;

                    try {
                        rs = conn.retrieveData(conn.getConnectionNoMultipleQueries(),query); //connect without allowing multiple queries
                        rsMetaData = rs.getMetaData();
                        //if db was not set and select is run on server, get db name from result set and set it to be able to get pkcolumn name
                        if(conn.getDB().equals("")) {
                            conn.setDB(rsMetaData.getCatalogName(1));//set db to
                            db_empty = true;
                        }

                        uniqueColumnName = conn.getUniqueColumn(conn.getConnection(), rsMetaData.getTableName(1));

                        //in case table has no primary key, nor a unique column
                        if(uniqueColumnName == null)
                            noUnique = true;

                        //in case db was not set and select query was run on server
                        if(db_empty)
                            conn.setDB("");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        success = false;
                        error = e.getMessage();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        success = false;
                    }


                    final boolean finalNoUnique = noUnique;
                    sqlView.post(new Runnable() {
                        @Override
                        public void run() {

                            if (success) {

                                //clear tablelayout child views
                                if(((TableLayout) result_table).getChildCount() > 0)
                                    ((TableLayout) result_table).removeAllViews();

                                if(finalNoUnique == true){
                                    TextView notice = new TextView(getActivity().getApplicationContext());
                                    notice.setText("This table does not contain a unique column. Edit and Delete features are not available.");
                                    result_table.addView(notice);//add textview directly above first row
                                }

                                int columnCount = 0;

                                try {
                                    columnCount = rsMetaData.getColumnCount();
                                    TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);

                                    //add TextViews column names (table header)
                                    TableRow trHead = new TableRow(getActivity().getApplicationContext());
                                    for (int i = 1; i <= columnCount; i++) {
                                        TextView tv = new TextView(getActivity().getApplicationContext());
                                        tv.setLayoutParams(lp);
                                        tv.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.cell_shape));
                                        tv.setGravity(Gravity.CENTER);
                                        tv.setPadding(5, 5, 5, 5);
                                        tv.setTextColor(Color.BLACK);
                                        tv.setText(rsMetaData.getColumnName(i));
                                        trHead.addView(tv);                                    }

                                    result_table.addView(trHead);

                                    while(rs.next()) {

                                        TableRow tr = new TableRow(getActivity().getApplicationContext());

                                        //add TextViews for each column of data
                                        for (int i = 1; i <= columnCount; i++) {
                                            TextView tv = new TextView(getActivity().getApplicationContext());
                                            tv.setLayoutParams(lp);
                                            tv.setBackgroundDrawable(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.cell_shape));
                                            tv.setGravity(Gravity.CENTER);
                                            tv.setPadding(5, 5, 5, 5);
                                            tv.setTextColor(Color.BLACK);
                                            String value = rs.getString(i);
                                            //if the column has no value (NULL)
                                            if(value == null)
                                                tv.setText("NULL");
                                            else
                                                tv.setText(value);

                                            tr.addView(tv);
                                        }

                                        final String tableName = rsMetaData.getTableName(1);

                                        //if there is a primary key or a unique, get its value, then add an edit and a delete button
                                        if(finalNoUnique == false) {

                                            final String PkColumnNameValue = rs.getString(uniqueColumnName);

                                            //add button edit
                                            Button bEdit = new Button(getActivity().getApplicationContext());
                                            bEdit.setText("Edit");
                                            bEdit.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    goToInsert(tableName, uniqueColumnName, PkColumnNameValue);
                                                }
                                            });
                                            tr.addView(bEdit);
                                            //end add button edit

                                            //add button delete row
                                            Button bDel = new Button(getActivity().getApplicationContext());
                                            bDel.setText("Delete");
                                            bDel.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {

                                                    //execute delete inside a thread to avoid network on main thread
                                                    new Thread(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            //execute delete row
                                                            try {
                                                                conn.execQuery(conn.getConnection(), "DELETE from " + tableName + " where " + uniqueColumnName + " = " + PkColumnNameValue);
                                                            } catch (SQLException e) {
                                                                e.printStackTrace();
                                                            } catch (ClassNotFoundException e) {
                                                                e.printStackTrace();
                                                            }

                                                            //refresh the result_table list
                                                            sqlView.post(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (((TableLayout) result_table).getChildCount() > 0)
                                                                        ((TableLayout) result_table).removeAllViews();

                                                                    execSQL(input_query.getText().toString());
                                                                }
                                                            });
                                                        }
                                                    }).start();
                                                }
                                            });
                                            tr.addView(bDel);
                                            //end add button delete row
                                        }//end if(noPK == false)

                                        //add row to table
                                        result_table.addView(tr);

                                    }//end loop on result set
                                } catch (SQLException e) {
                                    e.printStackTrace();
                                }

                                Toast.makeText(getActivity().getApplicationContext(), "Select query was successful", Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(getActivity().getApplicationContext(),error,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        }

        //insert / DDL Query
        if(radioGroup_queryType.getCheckedRadioButtonId() == R.id.radioButton_otherQueries){

            //execute query in a thread
            new Thread(new Runnable() {

                boolean success = true;
                String error;

                @Override
                public void run() {

                    try {
                        conn.execQuery(conn.getConnection(),query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                        success = false;
                        error = e.getMessage();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        success = false;
                    }

                    sqlView.post(new Runnable() {
                        @Override
                        public void run() {

                            if (success)
                                Toast.makeText(getActivity().getApplicationContext(),"Operation was successful",Toast.LENGTH_LONG).show();
                            else
                                Toast.makeText(getActivity().getApplicationContext(),error,Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        }


    }

    protected void goToInsert(String tName,String pkName,String pkVal){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new insertEditFrag();
        Bundle data = new Bundle();
        data.putString("tableName",tName);
        data.putString("pkName",pkName);
        data.putString("pkValue",pkVal);
        data.putBoolean("isNew", false);
        f.setArguments(data);
        transaction.replace(R.id.root_frame, f, "insertEditFrag");
        transaction.commit();

        //navigate to Tables tab
        ViewPager parentViewPager=(ViewPager) getActivity().findViewById(R.id.viewpager);
        parentViewPager.setCurrentItem(1);
    }

}
