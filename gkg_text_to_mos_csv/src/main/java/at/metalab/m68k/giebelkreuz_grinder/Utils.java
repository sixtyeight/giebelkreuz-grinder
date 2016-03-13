package at.metalab.m68k.giebelkreuz_grinder;

public class Utils {
	public static void assertNull(Object object) {
		if(object != null) {
			// throw new IllegalStateException("object not null");
			System.out.println("object not null: " + object);
		}
	}
}
