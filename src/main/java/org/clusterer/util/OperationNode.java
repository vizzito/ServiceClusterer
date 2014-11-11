package org.clusterer.util;

import org.ow2.easywsdl.wsdl.api.Operation;

public class OperationNode {
	private Operation op;
	private DataTypeNode input;
	private DataTypeNode output;
	private int numCluster;
	
	public OperationNode(Operation op) {
		this.op = op;
	}
	
	public void setInput(DataTypeNode input) {
		this.input = input;
	}
	
	public void setOutput(DataTypeNode output) {
		this.output = output;
	}
	
	public Operation getInternalOperation() {
		return op;
	}
	
	public DataTypeNode getInput() {
		return input;
	}
	
	public DataTypeNode getOutput() {
		return output;
	}

	public int getNumCluster() {
		return numCluster;
	}

	public void setNumCluster(int numCluster) {
		this.numCluster = numCluster;
	}
}
