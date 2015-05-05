package gaia.cu9.ari.gaiaorbit.gui.swing.components;

import javax.swing.Icon;
import javax.swing.tree.DefaultMutableTreeNode;

public class IconTreeNode extends DefaultMutableTreeNode {
    private Icon icon;

    public IconTreeNode(Object arg0, boolean arg1) {
        super(arg0, arg1);
    }

    public IconTreeNode(Object arg0, boolean arg1, Icon icon) {
        super(arg0, arg1);
        this.icon = icon;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }
}
