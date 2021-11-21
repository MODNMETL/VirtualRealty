package me.plytki.virtualrealty.enums;

public class Flag {

    public enum World {

        BLOCK_PLACE(false),
        BLOCK_BREAK(false),
        INTERACT(false),
        IGNITE(false),
        EXPLOSION_PRIME(false),
        ENTITY_DAMAGE(false),
        ARMOR_STAND_MANIPULATION(false),
        ITEM_FRAME_DESTROY(false),
        ITEM_FRAME_ROTATE(false);

        private boolean allowed;

        World(boolean allowed) {
            this.allowed = allowed;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }

    }

}
