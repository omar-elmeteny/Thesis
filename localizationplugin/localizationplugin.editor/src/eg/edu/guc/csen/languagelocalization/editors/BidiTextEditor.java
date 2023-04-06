package eg.edu.guc.csen.languagelocalization.editors;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

public class BidiTextEditor extends TextEditor {
    
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        var viewer =  super.createSourceViewer(parent, ruler, styles);
        viewer.getTextWidget().setOrientation(SWT.RIGHT_TO_LEFT);
        return viewer;
    }
}
