package com.yogur.pocketadmin;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by abed on 12/30/15.
 */
public class tableList extends Fragment implements View.OnClickListener{

    private View tableListView;
    private TableLayout list_tables;
    private Button button_create_table,button_drop_tables,button_truncate_tables;
    private EditText input_create_table,input_number_columns;
    private create_connection conn;
    private List<CheckBox> checkBoxes;
    private List<String> tableNames;
    private String dbName;

    public void updateTableList(){

        //re-update selected dbname
        dbName = ((MyApplication) getActivity().getApplication()).getDbName();

        if(((TableLayout) list_tables).getChildCount() > 0)
            ((TableLayout) list_tables).removeAllViews();

        populateTableList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        tableListView = inflater.inflate(R.layout.fragment_table_list,container,false);
        list_tables = (TableLayout) tableListView.findViewById(R.id.table_layout_table_list);
        button_create_table = (Button) tableListView.findViewById(R.id.button_create_table);
        button_create_table.setOnClickListener(this);
        button_drop_tables = (Button) tableListView.findViewById(R.id.button_drop_tables);
        button_drop_tables.setOnClickListener(this);
        button_truncate_tables = (Button) tableListView.findViewById(R.id.button_truncate_tables);
        button_truncate_tables.setOnClickListener(this);
        input_create_table = (EditText) tableListView.findViewById(R.id.input_create_table);
        input_number_columns = (EditText) tableListView.findViewById(R.id.input_number_columns);

        dbName = ((MyApplication) getActivity().getApplication()).getDbName();
        conn = ((MyApplication) getActivity().getApplication()).getConn();

        checkBoxes = new ArrayList<>();
        tableNames = new ArrayList<>();

        //may not need if here since db will not be selected at app run
        if(dbName.equals("")){
            TextView tv = new TextView(getActivity().getApplicationContext());
            tv.setText("No database selected");
            list_tables.addView(tv);
        } else {

            populateTableList();
        }
                return tableListView;
            }

    private void processSelectedTables(String QUERY) {

        String process_query = "";

        for (int j = 0; j < tableNames.size(); j++) {

            if (checkBoxes.get(j).isChecked())
                process_query += QUERY + " " + tableNames.get(j) + ";";
        }

        if (!process_query.equals("")) {

            final String finalProcess_query = process_query;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        conn.execQuery(conn.getConnection(), finalProcess_query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    //remove children of tablelayout and refresh it
                    tableListView.post(new Runnable() {
                        @Override
                        public void run() {

                            if (((TableLayout) list_tables).getChildCount() > 0) {
                                ((TableLayout) list_tables).removeAllViews();
                                populateTableList();
                            }
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(getActivity().getApplicationContext(), "No tables selected", Toast.LENGTH_LONG).show();
        }
    }

            @Override
            public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
        }

            public void populateTableList() {

                final TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
                checkBoxes.clear();

                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        try {
                            tableNames = conn.showTables(conn.getConnection());

                            tableListView.post(new Runnable() {
                                @Override
                                public void run() {
                                    //table header
                                    TableRow top_row = new TableRow(getActivity().getApplicationContext());
                                    top_row.setLayoutParams(lp);
                                    TextView empty_tv = new TextView(getActivity().getApplicationContext());
                                    top_row.addView(empty_tv);
                                    TextView db_tag = new TextView(getActivity().getApplicationContext());
                                    db_tag.setText("Table");
                                    top_row.addView(db_tag);
                                    list_tables.addView(top_row);
                                    //end table header

                                    for (int i = 0; i < tableNames.size(); i++) {

                                        final String tableName = tableNames.get(i);

                                        TableRow row = new TableRow(getActivity().getApplicationContext());
                                        row.setLayoutParams(lp);

                                        CheckBox checkBox = new CheckBox(getActivity().getApplicationContext());
                                        checkBoxes.add(checkBox);
                                        row.addView(checkBox);

                                        TextView tvTableName = new TextView(getActivity().getApplicationContext());
                                        tvTableName.setText(tableName);
                                        row.addView(tvTableName);

                                        Button bBrowse = new Button(getActivity().getApplicationContext());
                                        bBrowse.setId(i + 1);
                                        bBrowse.setLayoutParams(lp);
                                        bBrowse.setText("browse");
                                        bBrowse.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {

                                                sqlFragment frg_sqlFragment = (sqlFragment) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":2");
                                                frg_sqlFragment.setRadioButtonSelectChecked();
                                                String q = "SELECT * FROM " + tableName + " LIMIT 30";
                                                frg_sqlFragment.setInput_query(q);
                                                frg_sqlFragment.execSQL(q);

                                                //navigate to Tables tab
                                                ViewPager parentViewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
                                                parentViewPager.setCurrentItem(2);
                                            }
                                        });
                                        row.addView(bBrowse);

                                        //if button edit/view structure of table is pressed
                                        Button bStructure = new Button(getActivity().getApplicationContext());
                                        bStructure.setId(i + 1);
                                        bStructure.setLayoutParams(lp);
                                        bStructure.setText("structure");
                                        final int indexStructure = i;
                                        bStructure.setOnClickListener(new View.OnClickListener() {
                                            public void onClick(View v) {

                                                navigateTableStructure(tableName);
                                            }
                                        });
                                        row.addView(bStructure);

                                        //button insert into table
                                        Button bInsert = new Button(getActivity().getApplicationContext());
                                        bInsert.setText("insert");
                                        bInsert.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                navigateInsert(tableName);
                                            }
                                        });
                                        row.addView(bInsert);

                                        //add row to table
                                        list_tables.addView(row);
                                    }

                                    //table footer
                                    TableRow footer_row = new TableRow(getActivity().getApplicationContext());
                                    top_row.setLayoutParams(lp);
                                    TextView empty_tv_two = new TextView(getActivity().getApplicationContext());
                                    footer_row.addView(empty_tv_two);
                                    TextView table_count = new TextView(getActivity().getApplicationContext());
                                    table_count.setText("Total: " + tableNames.size());
                                    footer_row.addView(table_count);
                                    list_tables.addView(footer_row);
                                    //end table footer
                                }
                            });

                        } catch (SQLException e) {
                            e.printStackTrace();
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

    protected void navigateCreateTables(){

        String newTableName = input_create_table.getText().toString();

        if(dbName.equals("")){
            Toast.makeText(getActivity().getApplicationContext(),"No database is selected",Toast.LENGTH_LONG).show();
        } else if(newTableName.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(),"Please enter a table name",Toast.LENGTH_LONG).show();
        }
        else {
            //replace fragment in root fragment with createTableFrag
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            Fragment f = new createTableFrag();
            Bundle data = new Bundle();
            int numColumns = Integer.parseInt(input_number_columns.getText().toString());
            data.putString("newTableName",newTableName);
            data.putInt("numColumns",numColumns);
            f.setArguments(data);
            transaction.replace(R.id.root_frame, f, "createTablefrag");
            transaction.commit();
        }
    }

    protected void navigateTableStructure(String tableName){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new tableStructFrag();
        Bundle data = new Bundle();
        data.putString("tableName", tableName);
        f.setArguments(data);
        transaction.replace(R.id.root_frame,f,"tableStructFrag");
        transaction.commit();
    }

    protected void navigateInsert(String tableName){

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment f = new insertEditFrag();
        Bundle data = new Bundle();
        data.putString("tableName",tableName);
        data.putBoolean("isNew", true);
        f.setArguments(data);
        transaction.replace(R.id.root_frame,f,"insertEditFrag");
        transaction.commit();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.button_drop_tables:
                processSelectedTables("DROP TABLE");
                break;
            case R.id.button_truncate_tables:
                processSelectedTables("TRUNCATE");
                break;

            case R.id.button_create_table:
                navigateCreateTables();
                break;
        }
    }
}
