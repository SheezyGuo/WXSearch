package team.xinyuan519.wxsearch.utils;

/**
 * 
 * @author zhaoyi
 *
 */
public class Node {
	private double score;
	private String collName;
	private String remark;

	public double getScore() {
		return score;
	}

	public String getCollName() {
		return collName;
	}

	public String getRemark() {
		return remark;
	}
	
	public Node(){
		
	}

	public Node(String collName, double sc) {
		this.collName = collName;
		this.score = sc;
		this.remark = "";
	}

	public int compareTo(Node b) {
		if (this.score > b.score)
			return 1;
		if (this.score < b.score)
			return -1;
		return 0;
	}

	void sort(Node ns[]) {
		int n = ns.length;
		for (int i = n - 1; i >= 0; i--) {
			for (int j = 0; j < i; j++) {
				if (ns[j].compareTo(ns[j + 1]) < 0) {
					Node t = ns[j];
					ns[j] = ns[j + 1];
					ns[j + 1] = t;
				}
			}
		}
	}
}
