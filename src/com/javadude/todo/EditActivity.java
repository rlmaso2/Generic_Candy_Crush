package com.javadude.todo;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class EditActivity extends ActionBarActivity {
	private TodoItem item;
	private EditText name;
	private EditText description;
	private EditText priority;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		long todoItemId = getIntent().getLongExtra("todoItemId", -1);         /*!!!!!!!!*/
		
		name = (EditText) findViewById(R.id.name);
		description = (EditText) findViewById(R.id.description);
		priority = (EditText) findViewById(R.id.priority);
		
		if (todoItemId != -1) {
			item = TodoContentProvider.findTodo(this, todoItemId);            /*!!!!!!!!*/
		} else {
			item = new TodoItem("", "", 1);                                   /*!!!!!!!!*/
		}
			
			name.setText(item.getName());
			description.setText(item.getDisplayName());
			priority.setText(String.valueOf(item.getPriority()));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_cancel:
				finish();
				return true;
			case R.id.action_done:
				this.item.setName(name.getText().toString());
				this.item.setDisplayName(description.getText().toString());
				this.item.setPriority(Integer.parseInt(priority.getText().toString()));
				
				TodoContentProvider.updateTodo(this, this.item);              /*!!!!!!!!*/
				
				getIntent().putExtra("todoItemId", this.item.getId());        /*!!!!!!!!*/
				finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

}
