package makemachine.android.formgenerator;

import android.content.Context;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FormNumericEditText extends FormWidget
{
	protected TextView _label;
	protected EditText _input;
	protected int _priority;
		
	public FormNumericEditText(Context context, String property ) 
	{
		super( context, property );
		
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		
		_input = new EditText( context );
		_input.setInputType( InputType.TYPE_CLASS_PHONE );
		_input.setImeOptions( EditorInfo.IME_ACTION_DONE );
		_input.setLayoutParams( FormActivity.defaultLayoutParams );
		
		_layout.addView( _label );
		_layout.addView( _input );
	}

	public String getValue() {
		return _input.getText().toString();
	}

	public void setValue(String value) {
		_input.setText( value );
	}
	
	@Override 
	public void setHint( String value ){
		_input.setHint( value );
	}
}
