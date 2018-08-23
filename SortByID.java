import java.util.Comparator;

public class SortByID implements Comparator<Task> {
	@Override
	public int compare(Task t1, Task t2) {
		// TODO Auto-generated method stub
		return t1.getTaskID()-t2.getTaskID();
	}

}
