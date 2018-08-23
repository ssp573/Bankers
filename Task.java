import java.util.ArrayList;
import java.util.HashMap;


public class Task {
	private int task_id;
	int[] claim;
	int[] held;
	ArrayList<Object[]> process= new ArrayList<>();
	int cycles;
	int wait_time;
	int delay;
	boolean aborted;
	
	public Task(int task_num, ArrayList<Object[]> proc,int[] claims,int[] res_held){
		task_id=task_num;
		process= proc;
		claim=claims;
		held=res_held;
		cycles=0;
		wait_time=0;
		delay=0;
		aborted=false;
	}
	
	public Task(Task that) {
	       this.task_id = that.task_id;
	       this.process= that.process;
	       this.claim=that.claim;
	       this.held=that.held;
	       this.cycles=that.cycles;
	       this.wait_time=that.wait_time;
	       this.delay=that.delay;
	       aborted=true;
	   }
	public int getTaskID(){
		return task_id;
	}
	public void setTaskId(int id){
		task_id=id;
	}
	
}
