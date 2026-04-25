package pacmen.map;

import pacmen.entities.Entity;

public interface Cell {
    /**
     * Determines if an entity (Player or Ghost) can enter a given cell.
     * Some cells let ghosts pass through but not players (e.g. Ghost 
     * House Door), so we need to pass the entity
     * @param entity the entity (Player or Ghost)
     * @return true if can pass, false otherwise
     */
    public boolean canPass(Entity entity);

    /**
     * Triggers a cell's action when an entity successfully enters it
     * The action depends on the cell type
     * @param entity the entity (Player or Ghost)
     */
    public void onSteppedOn(Entity entity);
}
