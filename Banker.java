import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;


public class Banker {
	public static ArrayList<Task> Tasks=new ArrayList<Task>();
	public static void main(String[] args) throws IOException{
		//gathering input from file
		File file;
		Scanner sc;
		String filename="";
		if (args.length>0){
				filename=args[0];
		}
		file=new File(filename);
		sc=new Scanner(file);
		int num_tasks=sc.nextInt();
		int num_resources=sc.nextInt();
		int resources_available[]=new int[num_resources];
		for (int i=0;i<num_resources;i++){
			resources_available[i]=sc.nextInt();
		}
		//creating tasks and adding them to the Task list
		for (int i=0; i<num_tasks; i++){
			ArrayList<Object[]> proc= new ArrayList<>();
			int[] claim=new int[num_resources];
			int[] held=new int[num_resources];
			Task t= new Task(i+1,proc,claim,held);
			Tasks.add(t);
		}
		//adding each instruction in the process of the task
		while (sc.hasNextLine()){
			String instr=sc.next();
			int task_num = sc.nextInt();
			int delay= sc.nextInt();
			int res_num= sc.nextInt();
			int required= sc.nextInt();
			Object[] process={instr,delay,res_num,required};
			for ( Task t : Tasks) {
				if (t.getTaskID()==task_num){
					t.process.add(process);
				}
			}
		}
		sc.close();
		//call for fifo
		fifo(num_tasks, num_resources, Tasks, resources_available);
		Scanner sc1=new Scanner(file);
		sc1.nextLine();
		//adding the instructions again as they are exhausted in FIFO
		while (sc1.hasNextLine()){
			String instr=sc1.next();
			int task_num = sc1.nextInt();
			int delay= sc1.nextInt();
			int res_num= sc1.nextInt();
			int required= sc1.nextInt();
			Object[] process={instr,delay,res_num,required};
			for ( Task t : Tasks) {
				if (t.getTaskID()==task_num){
					t.process.add(process);
				}
			}
		}
		System.out.println("------------------------------");
		sc1.close();
		//call for banker's
		banker(num_tasks, num_resources, Tasks, resources_available);
	}
	
	//function for the FIFO algorithm
	static void fifo(int t, int r, ArrayList<Task> Tasks, int[] res){
		int terminated=0;
		ArrayList<Task> terminated_tasks=new ArrayList<>();
		ArrayList<Task> WaitingTasks=new ArrayList<Task>();
		ArrayList<Task> BlockedTasks=new ArrayList<Task>();
		ArrayList<Task> LocalTasks=new ArrayList<Task>();
		for (Task tsk: Tasks){
			Task tk=new Task(tsk);
			LocalTasks.add(tk);
		}
		//Waiting List is the list of non-blocked tasks which are waiting to be processed
		WaitingTasks.addAll(LocalTasks);
		int cycle= 0;
		//running the algorithm till all tasks are terminated
		while(terminated!=t){
			ArrayList<Task> temp=new ArrayList<>();
			ArrayList<Task> temp1=new ArrayList<>();
			int[] released_resources=new int[r];
			// initializing released resources to 0
			for (int i=0;i<r;i++){
				released_resources[i]=0;
			}
			//Handling blocked tasks first
			for (Task tsk: BlockedTasks){
				Object[] curr=tsk.process.get(0);
				String inst=(String) curr[0];
				int delay = (Integer) curr[1];
				int res_type = (Integer) curr[2]-1;
				int req = (Integer) curr[3];
				//delay handling
				if (tsk.delay<delay){
					tsk.delay++;
					tsk.cycles++;
				}
				else{
					//handling each instruction according to the first element of the instruction
					if (inst.equals("initiate")){
						tsk.held[res_type]=0;
						tsk.cycles++;
						tsk.process.remove(curr);
					}
					if (inst.equals("request")){
						if (req>res[res_type]){
							tsk.cycles++;
							tsk.wait_time++;
							
						}
						else{
							tsk.held[res_type]+=req;
							temp1.add(tsk);
							res[res_type]-=req;
							tsk.cycles++;
							tsk.process.remove(curr);
						}
					}
					if (inst.equals("release")){
						tsk.held[res_type]-=req;
						released_resources[res_type]+=req;
						tsk.cycles++;
						tsk.process.remove(curr);
					}
					if (inst.equals("terminate")){
						terminated++;
						tsk.process.remove(curr);
						terminated_tasks.add(tsk);
					}
				}
			}
			//removing from BlockedList the tasks which have been granted request
			BlockedTasks.removeAll(temp1);
			//handling the normal waiting list
			for (Task tsk: WaitingTasks){
				Object[] curr=tsk.process.get(0);
				String inst=(String) curr[0];
				int delay = (Integer) curr[1];
				int res_type = (Integer) curr[2]-1;
				int req = (Integer) curr[3];
				//delay handling
				if (tsk.delay<delay){
					tsk.delay++;
					tsk.cycles++;
				}
				else{
					//handling each instruction according to the first element of the instruction
					tsk.delay=0;
					if (inst.equals("initiate")){
						tsk.held[res_type]=0;
						tsk.cycles++;
						tsk.process.remove(curr);
					}
					if (inst.equals("request")){
						if (req>res[res_type]){
							BlockedTasks.add(tsk);
							tsk.cycles++;
							tsk.wait_time++;
							temp.add(tsk);
						}
						else{
							tsk.held[res_type]+=req;
							res[res_type]-=req;
							tsk.cycles++;
							tsk.process.remove(curr);
						}
					}
					if (inst.equals("release")){
						tsk.held[res_type]-=req;
						released_resources[res_type]+=req;
						tsk.cycles++;
						tsk.process.remove(curr);
					}
					if (inst.equals("terminate")){
						terminated++;
						tsk.process.remove(curr);
						terminated_tasks.add(tsk);
					}
				}
			}
			WaitingTasks.addAll(temp1); //adding to waiting list the tasks in the blocked list that have been granted request in this cycle 
			//restoring released resources to the list of available resources
			for (int i=0;i<r;i++){
				res[i]+=released_resources[i];
			}
			//resetting list of released resources to be zero
			for (int i=0;i<r;i++){
				released_resources[i]=0;
			}
			//removing from Waiting tasks all tasks which are blocked or terminated in this cycle
			WaitingTasks.removeAll(temp);
			WaitingTasks.removeAll(terminated_tasks);
			ArrayList<Task> temp3=new ArrayList<Task>();
			//Deadlock Handling
			if (BlockedTasks.size()==t-terminated && terminated!=t){
				int res_type=(int)BlockedTasks.get(0).process.get(0)[2]-1;
				int resource_available=res[res_type];
				//minimum request to satisfy
				int[] request=new int[t];
				boolean not_deadlocked=false;
				Collections.sort(BlockedTasks,new SortByID()); //sorting with respect to task ID for proper ordering
				//getting the value of minimum request
				for (Task tsk:BlockedTasks){
					int i=tsk.getTaskID()-1;
					request[i]=(int)tsk.process.get(0)[3];
				}
				//aborting tasks
				for (Task tsk: BlockedTasks){
					tsk.cycles=-1;
					resource_available+=tsk.held[res_type];
					for (int i=0;i<r;i++){
					res[i]+=tsk.held[i];
						tsk.held[i]=0;
					}
					//nullify request made by the process
					request[tsk.getTaskID()-1]=0;
					tsk.process.clear();
					terminated++;
					terminated_tasks.add(tsk);
					temp3.add(tsk);
					//checking if minimum request is greater than what we have
					//not_deadlocked is set to true if any of the tasks can be satisfied
					for (int i=0;i<t;i++){
						//System.out.println("res_type is "+res_type);
						if (request[i]<=resource_available && request[i]!=0){
							not_deadlocked=true;
						}
					}
					if(not_deadlocked){
						break;	//stop aborting tasks if there is no more a deadlock
					}
				}
				//removing all aborted tasks from Blocked List
				BlockedTasks.removeAll(temp3);
				//adding the rest of the Blocked List to Waiting list and clearing Blocked List
				WaitingTasks.addAll(BlockedTasks);
				BlockedTasks.clear();
			}
			cycle++;
		}
		Collections.sort(terminated_tasks,new SortByID());
		printOutput(terminated_tasks,"FIFO");
	}
	
	
	//function to print output for one algorithm
	static void printOutput(ArrayList<Task> localTasks, String st){
		System.out.println("\t"+st);
		double total_cycles = 0,total_wait=0;
		for (Task t:localTasks){
			System.out.print("Task "+t.getTaskID());
			//cycles are set to -1 to indicate aborted tasks
			if (t.cycles==-1){
				System.out.println("\taborted");
			}
			else{
				total_cycles+=t.cycles;
				total_wait+=t.wait_time;
				System.out.println("\t"+t.cycles+"\t"+t.wait_time+"\t"+Math.round(((double)t.wait_time/(double)t.cycles)*100)+"%");
			}
		}
		System.out.println("Total"+"\t"+(int)total_cycles+"\t"+(int)total_wait+"\t"+Math.round((total_wait/total_cycles)*100)+"%");
	}
	
	//Function for Banker's Algorithm
	static void banker(int t, int r, ArrayList<Task> Tasks, int[] res){
		int terminated=0;
		int cycle=0;
		ArrayList<Task> terminated_tasks=new ArrayList<>();;
		ArrayList<Task> WaitingTasks=new ArrayList<Task>();
		ArrayList<Task> BlockedTasks=new ArrayList<Task>();
		ArrayList<Task> LocalTasks=new ArrayList<Task>();
		ArrayList<Task> claimFault=new ArrayList<Task>();
		for (Task tsk: Tasks){
			Task tk=new Task(tsk);
			LocalTasks.add(tk);
		}
		LocalTasks.removeAll(claimFault);
		WaitingTasks.addAll(LocalTasks);
		while(terminated!=t){
			ArrayList<Task> temp=new ArrayList<>();
			ArrayList<Task> temp1=new ArrayList<>();
			int[] released_resources=new int[r];
			for (int i=0;i<r;i++){
				released_resources[i]=0;
			}
			for (Task tsk: BlockedTasks){
				Object[] curr=tsk.process.get(0);
				String inst=(String) curr[0];
				int delay = (Integer) curr[1];
				int res_type = (Integer) curr[2]-1;
				int req = (Integer) curr[3];
				//same as FIFO except for error checks on initiate and request. Also includes a call to function safe() which checks if the resulting state after the request is safe
					if (inst.equals("initiate")){
						if (res[res_type]>=req){
							tsk.claim[res_type]=req;
							tsk.held[res_type]=0;
							tsk.cycles++;
							tsk.process.remove(curr);
						}
						else{
							claimFault.add(tsk);
							terminated++;
							tsk.cycles=-1;
							released_resources[res_type]+=req;
							terminated_tasks.add(tsk);
						}
					}
					if (inst.equals("request")){
						if (req>tsk.claim[res_type]){
							claimFault.add(tsk);
							released_resources[res_type]+=req;
							terminated++;
							tsk.cycles=-1;
							terminated_tasks.add(tsk);
						}
						else{
							if (req>res[res_type]){
								tsk.cycles++;
								tsk.wait_time++;
								
							}
							else{
								if (safe(LocalTasks,terminated_tasks,req,res_type,tsk.getTaskID(),res, r, t)){
									tsk.held[res_type]+=req;
									temp1.add(tsk);
									res[res_type]-=req;
									tsk.claim[res_type]-=req;
									tsk.cycles++;
									tsk.process.remove(curr);
								}
								else{
										tsk.cycles++;
										tsk.wait_time++;
								}
							}
						}
					}
					if (inst.equals("release")){
						tsk.held[res_type]-=req;
						released_resources[res_type]+=req;
						tsk.cycles++;
						tsk.process.remove(curr);
						tsk.claim[res_type]+=req;
					}
					if (inst.equals("terminate")){
						terminated++;
						tsk.process.remove(curr);
						terminated_tasks.add(tsk);
						System.out.println("Ended at cycle "+tsk.cycles);
					}
			}
			BlockedTasks.removeAll(temp1);
			//removing all tasks which had a claim Fault or a Request Fault
			BlockedTasks.removeAll(claimFault);
			
			for (Task tsk: WaitingTasks){
				Object[] curr=tsk.process.get(0);
				String inst=(String) curr[0];
				int delay = (Integer) curr[1];
				int res_type = (Integer) curr[2]-1;
				int req = (Integer) curr[3];
				
				if (tsk.delay<delay){
					tsk.delay++;
					tsk.cycles++;
				}
				else{
					//same as FIFO except for error checks on initiate and request. Also includes a call to function safe() which checks if the resulting state after the request is safe
					tsk.delay=0;
					if (inst.equals("initiate")){
						if (res[res_type]>=req){
							tsk.claim[res_type]=req;
							tsk.held[res_type]=0;
							tsk.cycles++;
							tsk.process.remove(curr);
						}
						else{
							System.out.println("Banker aborts task "+tsk.getTaskID()+" before run begins:\n\tclaim for resource "+(res_type+1)+"("+req+") exceeds number of units present "+res[res_type]);
							claimFault.add(tsk);
							terminated++;
							terminated_tasks.add(tsk);
							tsk.cycles=-1;
						}
					}
					if (inst.equals("request")){
						if (req>tsk.claim[res_type]){
							System.out.println("During cycle "+cycle+"-"+(cycle+1)+" of Banker's algorithm\n\tTask "+tsk.getTaskID()+"'s request exceeds its claim; aborted; "+req +" units available next cycle");
							claimFault.add(tsk);
							terminated++;
							released_resources[res_type]+=req;
							terminated_tasks.add(tsk);
							tsk.cycles=-1;
						}
						else{	
							if (req>res[res_type]){
								BlockedTasks.add(tsk);
								tsk.cycles++;
								tsk.wait_time++;
								temp.add(tsk);
							}
							else{
								if(safe(LocalTasks,terminated_tasks,req,res_type,tsk.getTaskID(),res, r, t)){
									tsk.held[res_type]+=req;
									res[res_type]-=req;
									tsk.claim[res_type]-=req;
									tsk.cycles++;
									tsk.process.remove(curr);
								}
								else{
										BlockedTasks.add(tsk);
										tsk.cycles++;
										tsk.wait_time++;
										temp.add(tsk);
								}
							}
						}
					}
					if (inst.equals("release")){
						tsk.held[res_type]-=req;
						released_resources[res_type]+=req;
						tsk.cycles++;
						tsk.process.remove(curr);
						tsk.claim[res_type]+=req;
					}
					if (inst.equals("terminate")){
						terminated++;
						tsk.process.remove(curr);
						terminated_tasks.add(tsk);
					}
				}
			}
			//removing all tasks which had a Claim Error or Request Error
			WaitingTasks.removeAll(claimFault);
			WaitingTasks.addAll(temp1);
			
			for (int i=0;i<r;i++){
				res[i]+=released_resources[i];
			}
			for (int i=0;i<r;i++){
				released_resources[i]=0;
			}
			WaitingTasks.removeAll(temp);
			WaitingTasks.removeAll(terminated_tasks);
			
			cycle++;
		}
		Collections.sort(terminated_tasks,new SortByID());
		printOutput(terminated_tasks,"BANKER'S");
	}

	//Function to check if the resulting state if the request is granted
	public static boolean safe(ArrayList<Task> LocalTasks,ArrayList<Task> terminated, int req,int res_type,int tsk_id,int[] res, int r, int t){
		ArrayList<Task> clonedTasks = new ArrayList<Task>();
		ArrayList<Task> clonedTerminated= new ArrayList<Task>();
		int[] clonedRes=new int[res.length];
		//resource cloning to operate on
		for (int i=0; i<r; i++){
			clonedRes[i]=res[i];
		}
		Task to_Test=null;  //pointer for task for which safe has been called
		
		//cloning attributes for each task. Cloning is done in order to not disturb the original state as this is only a simulation to see if the resulting state is safe.
		for (Task tk:LocalTasks){
			//only for Tasks which aren't aborted
			if (tk.cycles!=-1){
				int tkid=tk.getTaskID();
				int[] claim=new int[tk.claim.length];
				for (int i=0;i<tk.claim.length;i++){
					claim[i]=tk.claim[i];
				}
				int[] held=new int[tk.held.length];
				for (int i=0;i<tk.held.length;i++){
					held[i]=tk.held[i];
				}
				ArrayList<Object[]> proc=new ArrayList<Object[]>();
				for (Object[] o:tk.process){
					Object[] c=new Object[o.length];
					for (int i =0; i<o.length; i++){
						c[i]=o[i];
					}
					proc.add(c);
				}
				Task task=new Task(tkid, proc, claim, held);  //creating new task from cloned attributes
				int delays;
				delays=tk.delay;
				task.delay=delays;
				if (tk.getTaskID()==tsk_id){
					to_Test=task;
				}
				if (terminated.contains(tk)){
					clonedTerminated.add(task);
				}
				else{
					clonedTasks.add(task);
				}
			}
		}
		
		to_Test.held[res_type]+=req;		//assuming that resource requested is granted
		clonedRes[res_type]-=req;
		to_Test.claim[res_type]-=req;
		
		//checking if the task operated upon is releasing it's last resource. If so, remove from the clonedTasks list. This is done because as claim is 0, it causes the further parts to malfunction
		
		boolean done=true;
		for (int i=0;i<r;i++){
			if (to_Test.claim[i]>0){
				done=false;
			}
		}
		if (done){
			for (int i=0;i<r;i++){
				clonedRes[i]+=to_Test.held[i];
				to_Test.held[i]=0;
				clonedTasks.remove(to_Test);
			}	
		}

		//checking if any process is terminated. If so, removed.
		ArrayList<Task> temp = new ArrayList<Task>();
		for (Task tsk:clonedTasks){
			boolean all_zero=true;
			for (int i=0;i<r;i++){
				if (tsk.claim[i]>0){
					all_zero=false;
				}
			}
			if (all_zero){
				temp.add(tsk);
			}
		}
		clonedTasks.removeAll(temp);
		int count=0;
		int size=clonedTasks.size();
		
		while (count<size){
			Task next=null;
			int max=0;
			boolean locked=true;
			//checking if any of the tasks can be satisfied
			for (Task tk: clonedTasks){
				boolean satisfyable=true;
				for (int i=0;i<r;i++){
					if (tk.claim[i]>clonedRes[i]){
						satisfyable=false;
						break;
					}
				}
				//among the satisfyable tasks, the task with maximum claims is given the resources required and allowed to run to completion
				if (satisfyable){
					locked=false;
					int add=0;
					for (int i=0;i<r;i++){
						add+=tk.claim[i];
					}
					if (add>max){
						max=add;
						next=tk;
					}		
				}
			}
			//if no task is satisfyable, locked remains true and we break out of the while loop
			if (locked){
				break;
			}
			clonedTasks.remove(next);
			// the selected task gives back the resources after completion
			for (int i=0;i<r;i++){
				clonedRes[i]+=next.held[i];
				next.held[i]=0;
			}
			count++;
		}
		//if the entire while loop runs without breaking, that is all Tasks run and terminate, return true(state is safe). Else return false. 
		if (count==size){
			return true;
		}
		else{
			return false;
		}
	}
}
