package makemachine.android.formgenerator;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class FormEditText extends FormWidget
{
	protected TextView _label;
	protected EditText _input;
	
	public FormEditText( Context context, String property )
	{
		super( context, property );
		
		_label = new TextView( context );
		_label.setText( getDisplayText() );
		_label.setLayoutParams( FormActivity.defaultLayoutParams );
	
		_input = new EditText( context );
		_input.setLayoutParams( FormActivity.defaultLayoutParams );
		_input.setImeOptions( EditorInfo.IME_ACTION_DONE );
		
		_layout.addView( _label );
		_layout.addView( _input );
	}
	
	@Override
	public String getValue(){
		return _input.getText().toString();
	}
	
	@Override
	public void setValue( String value ) {
		_input.setText( value );
	}
	
	@Override 
	public void setHint( String value ){
		_input.setHint( value );
	}
}
