package railroad_simulation.exception;

public class InvalidConfigurationException extends Exception{
	public InvalidConfigurationException(){
		super("Invalid configuration");
	}

	public InvalidConfigurationException(String message){
		super(message);
	}
}
