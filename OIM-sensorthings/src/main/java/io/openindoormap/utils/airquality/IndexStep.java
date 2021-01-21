package io.openindoormap.utils.airquality;

public class IndexStep {
    private Index index;
    private double min;
    private double max;

    public IndexStep (Index index, double min, double max) {
        this.index = index;
        this.min = min;
        this.max = max;
    }

	public Index getIndex() {
		return index;
    }
    
	public void setIndex(Index index) {
		this.index = index;
    }
    
	public double getMin() {
		return min;
    }
    
	public void setMin(double min) {
		this.min = min;
    }
    
	public double getMax() {
		return max;
    }
    
	public void setMax(double max) {
		this.max = max;
	}
}
