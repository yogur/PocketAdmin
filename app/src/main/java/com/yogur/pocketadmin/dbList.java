package com.yogur.pocketadmin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
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
 * Created by abed on 12/24/15.
 */
public class dbList extends Fragment {

    private View frag_view;
    private create_connection c;
    private EditText input_create_db;
    private Button button_drop_dbs,button_create_db;
    private List<String> dbs;
    private List<CheckBox> checkBoxes;
    private TableLayout tl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        frag_view = inflater.inflate(R.layout.fragment_db_list,container,false);

        checkBoxes = new ArrayList<>();

        button_drop_dbs = (Button) frag_view.findViewById(R.id.button_drop_dbs);
        button_create_db = (Button) frag_view.findViewById(R.id.button_create_db);
        input_create_db = (EditText) frag_view.findViewById(R.id.input_create_db);

        tl = (TableLayout) frag_view.findViewById(R.id.table_layout_db);

        // get connection
        c = ((MyApplication) getActivity().getApplication()).getConn();

        //dynamically setup database list
        setupDatabaseList();

        button_create_db.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){

                createDb(input_create_db.getText().toString());
            }
        });

        //button drop selected databases
        button_drop_dbs.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                dropSelectedDatabases();
            }
        });

        return frag_view;
    }

    private void dropSelectedDatabases(){

        String drop_query = "";

        for(int j = 0; j < dbs.size(); j++){

            if(checkBoxes.get(j).isChecked())
                drop_query += "DROP DATABASE IF EXISTS " + dbs.get(j) + ";";
        }

        if(!drop_query.equals("")){

            final String finalDrop_query = drop_query;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        c.execQuery(c.getConnection(), finalDrop_query);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                    //remove children of tablelayout and refresh it
                    frag_view.post(new Runnable() {
                        @Override
                        public void run() {

                            if(((TableLayout) tl).getChildCount() > 0) {
                                ((TableLayout) tl).removeAllViews();
                                setupDatabaseList();
                            }
                        }
                    });
                }
            }).start();

        } else {
            Toast.makeText(getActivity().getApplicationContext(),"No databases selected",Toast.LENGTH_LONG).show();
        }
    }

    private void createDb(final String dbName){

        if(dbName.equals("")){
            Toast.makeText(getActivity().getApplicationContext(),"Database name cannot be empty.",Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(new Runnable() {

            boolean success = true;
            String error;

            @Override
            public void run() {

                try {
                    c.execQuery(c.getConnection(),"CREATE DATABASE "+dbName);
                } catch (SQLException e) {
                    e.printStackTrace();
                    success = false;
                    error = e.getMessage();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                frag_view.post(new Runnable() {
                    @Override
                    public void run() {

                        if(success){

                            //remove all views and restup
                            if(((TableLayout) tl).getChildCount() > 0)
                                ((TableLayout) tl).removeAllViews();
                            setupDatabaseList();

                            Toast.makeText(getActivity().getApplicationContext(),dbName + " created.",Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),error,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).start();
    }

    private void setupDatabaseList(){

        final TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        checkBoxes.clear();

        new Thread(new Runnable() {
            public void run() {

                try {
                    dbs = c.showDatabases(c.getConnection());

                    frag_view.post(new Runnable() {
                        public void run() {

                            //table header
                            TableRow top_row = new TableRow(getActivity().getApplicationContext());
                            top_row.setLayoutParams(lp);
                            TextView empty_tv = new TextView(getActivity().getApplicationContext());
                            top_row.addView(empty_tv);
                            TextView db_tag = new TextView(getActivity().getApplicationContext());
                            db_tag.setText("Database");
                            top_row.addView(db_tag);
                            tl.addView(top_row);
                            //end table header

                            for(int i = 0; i < dbs.size(); i++) {

                                TableRow row= new TableRow(getActivity().getApplicationContext());
                                row.setLayoutParams(lp);

                                CheckBox checkBox = new CheckBox(getActivity().getApplicationContext());
                                checkBoxes.add(checkBox);
                                row.addView(checkBox);

                                Button b = new Button(getActivity().getApplicationContext());
                                b.setId(i + 1);
                                b.setLayoutParams(lp);
                                b.setText(dbs.get(i));
                                final int index = i;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {

                                        String dbName = dbs.get(index);

                                        //set db in connection object and then in global class
                                        c.setDB(dbName);
                                        ((MyApplication) getActivity().getApplication()).setConn(c);
                                        ((MyApplication) getActivity().getApplication()).setDbName(dbs.get(index));

                                        /*
                                        // Reload sql fragment
                                        Fragment frg = null;
                                        frg = getActivity().getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":1");
                                        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                        ft.detach(frg);
                                        ft.attach(frg);
                                        ft.commit();
                                        */

                                        //tableList frg_tableList = (tableList) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":1");
                                        tableList frg_tableList = (tableList) getFragmentManager().findFragmentByTag("tableListFragment");
                                        //check if this fragment is currently viewed
                                        if (frg_tableList != null && frg_tableList.isVisible()) {
                                            frg_tableList.updateTableList();
                                        } else {
                                            FragmentTransaction transaction = getFragmentManager()
                                                    .beginTransaction();
                                            transaction.replace(R.id.root_frame, new tableList(),"tableListFragment");
                                            transaction.commit();
                                        }

                                        sqlFragment frg_sqlFragment = (sqlFragment) getFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":2");
                                        frg_sqlFragment.setTextView_sql(dbName);

                                        //navigate to Tables tab
                                        ViewPager parentViewPager=(ViewPager) getActivity().findViewById(R.id.viewpager);
                                        parentViewPager.setCurrentItem(1);

                                    }
                                });

                                row.addView(b);

                                tl.addView(row);
                            }

                            //table footer
                            TableRow footer_row = new TableRow(getActivity().getApplicationContext());
                            top_row.setLayoutParams(lp);
                            TextView empty_tv_two = new TextView(getActivity().getApplicationContext());
                            footer_row.addView(empty_tv_two);
                            TextView db_count = new TextView(getActivity().getApplicationContext());
                            db_count.setText("Total: "+dbs.size());
                            footer_row.addView(db_count);
                            tl.addView(footer_row);
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
}
