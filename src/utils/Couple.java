package utils;

public class Couple<T> {

	private T x;
	private T y;
	
	public Couple(T x, T y){
		this.x = x;
		this.y = y;
	}
	
	public T getX() {
		return x;
	}
	
	public T getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return "(" + x.toString() + ", " + y.toString() + ")";
	}
}
