package com.example.smartwellness;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DietDialog extends Dialog {
	private String mName;
	private EditText mAmount;
	private Button btn_ok;
	private Button btn_cancel;
	
	public DietDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_diet_dialog);
		
		((TextView)findViewById(R.id.diet_kind_textview)).setText(mName);
		mAmount = (EditText) findViewById(R.id.diet_edit_amount);
		btn_ok = (Button)findViewById(R.id.diet_ok_btn);
		btn_cancel = (Button)findViewById(R.id.diet_cancel_btn);
		
		btn_cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {	
				dismiss();
			}
		});
		
		btn_ok.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				mAmount.getText();
			}
		});
	}
}
