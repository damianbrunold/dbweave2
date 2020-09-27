import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.ImageIcon
import javax.swing.KeyStroke
import kotlin.system.exitProcess

class FileNew : AbstractAction("New") {
    init {
        putValue(MNEMONIC_KEY, 'N'.toInt())
        putValue(SHORT_DESCRIPTION, "Create a new pattern")
        putValue(SMALL_ICON, ImageIcon("images/sb_new.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO check for changes!
    }
}

class FileOpen : AbstractAction("Open") {
    init {
        putValue(MNEMONIC_KEY, 'O'.toInt())
        putValue(SHORT_DESCRIPTION, "Open a pattern file")
        putValue(SMALL_ICON, ImageIcon("images/sb_open.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO check for changes!
    }
}

class FileSave : AbstractAction("Save") {
    init {
        putValue(MNEMONIC_KEY, 'S'.toInt())
        putValue(SHORT_DESCRIPTION, "Save the pattern file")
        putValue(SMALL_ICON, ImageIcon("images/sb_save.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO
    }
}

class FileExit : AbstractAction("Exit") {
    init {
        putValue(MNEMONIC_KEY, 'x'.toInt())
        putValue(SHORT_DESCRIPTION, "Quit the application")
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F4"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO check for changes!
        exitProcess(0);
    }
}

class EditCopy : AbstractAction("Copy") {
    init {
        putValue(MNEMONIC_KEY, 'C'.toInt())
        putValue(SHORT_DESCRIPTION, "Copy contents to the clip board")
        putValue(SMALL_ICON, ImageIcon("images/mn_copy.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO
    }
}

class EditCut : AbstractAction("Cut") {
    init {
        putValue(MNEMONIC_KEY, 'u'.toInt())
        putValue(SHORT_DESCRIPTION, "Cut contents to the clip board")
        putValue(SMALL_ICON, ImageIcon("images/mn_cut.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO
    }
}

class EditPaste : AbstractAction("Paste") {
    init {
        putValue(MNEMONIC_KEY, 'P'.toInt())
        putValue(SHORT_DESCRIPTION, "Paste contents from the clip board")
        putValue(SMALL_ICON, ImageIcon("images/mn_paste.png"))
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"))
    }
    override fun actionPerformed(e: ActionEvent?) {
        // TODO
    }
}
