package org.clusterer.util;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.clusterer.similarity.OverlappingSimilarityFunction;
import org.ow2.easywsdl.schema.api.Element;
import org.ow2.easywsdl.schema.api.Type;
import org.ow2.easywsdl.schema.impl.ComplexTypeImpl;
import org.ow2.easywsdl.schema.impl.SimpleTypeImpl;
import org.ow2.easywsdl.wsdl.api.Operation;
import org.ow2.easywsdl.wsdl.api.Part;

public class DataTypeNode {
	private List<Operation> relatedOperations;
	private Element element;
	private List<Part> parts;
	private String parameterType;
	private Hashtable<QName, QName> flattenElements;
	
	public static final String INPUT = "input";
	public static final String OUTPUT = "output";
	
	public DataTypeNode(Element element) {
		relatedOperations = new LinkedList<Operation>();
		this.element = element;
	}
	
	public DataTypeNode(List<Part> parts) {
		relatedOperations = new LinkedList<Operation>();
		this.parts = parts;
	}
	
	public void addRelatedOperation(Operation operation) {
		if (!this.contains(operation))
			relatedOperations.add(operation);
	}
	
	public void addRelatedOperations(List<Operation> operations) {
		for (Operation op : operations) {
			addRelatedOperation(op);
		}
	}
	
	public List<Operation> getRelatedOperations() {
		return relatedOperations;
	}
	
	public Element getElement() {
		return element;
	}
	
	public List<Part> getAllElements() {
		return parts;
	}
	
	public void setElement(Element element) {
		this.element = element;
	}
	
	public Type getElementType() {
		return element.getType();
	}
	
	public String getParameterType() {
		return parameterType;
	}
	
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}
	
	public boolean isSimilar(DataTypeNode node, double threshold) {
		if (new OverlappingSimilarityFunction(threshold).getSimilarity(this, node) >= threshold)
			return true;
		return false;
	}
	
	@Override
	public String toString() {
		return element.getQName().getLocalPart() + " - " + parameterType;
	}
	
	public boolean contains(Operation op) {
		for (Operation relatedOp : relatedOperations)
			if (relatedOp.equals(op))
				return true;
		return false;
	}
	
	/**
	 * Returns a list of <element name, type name> pairs represented by QName objects.
	 * @return flattenElements
	 */
	public Hashtable<QName, QName> flattenDataTypes() {
		if (flattenElements == null) {
			flattenElements = new Hashtable<QName, QName>();
			if (parts != null) {
				for (Part part : parts) {
					flattenElement(part.getElement());
				}
			}
			//Just for testing purposes. We should use parts...
			else {
				flattenElement(element);
			}
		}
		return flattenElements;
	}
	
	/**
	 * Flatten elements in a recursive way. Duplicated elements
	 * are ignored. 
	 * @param element
	 */
	private void flattenElement(Element element) {
		//Is simple type
		if (element.getType() instanceof SimpleTypeImpl) {
			addElement(element);
		}
		else {
			//Is complex type
			ComplexTypeImpl complexType = (ComplexTypeImpl) element.getType();
			//If has sequence with elements
			if (complexType.hasSequence()) {
				//Flatten each element
				for (Element e : complexType.getSequence().getElements()) {
					flattenElement(e);
				}
			}
			//Has anonymous data type
			else {
				addElement(element);
			}
		}
	}
	
	/**
	 * Add an element if it does not exists within the list.
	 * @param element
	 */
	private void addElement(Element element) {
		if (element != null && !flattenElements.containsKey(element.getQName())) {
			flattenElements.put(element.getQName(), element.getType().getQName());
		}
	}
}
