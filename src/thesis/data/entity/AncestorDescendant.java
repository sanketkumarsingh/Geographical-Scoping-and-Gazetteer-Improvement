package thesis.data.entity;

import java.util.Set;

public class AncestorDescendant {

	Set<String> ascendants;

	public Set<String> getAscendants() {
		return ascendants;
	}

	public void setAscendants(Set<String> ascendants) {
		this.ascendants = ascendants;
	}

	public Set<String> getDescendants() {
		return descendants;
	}

	public void setDescendants(Set<String> descendants) {
		this.descendants = descendants;
	}

	Set<String> descendants;


}
