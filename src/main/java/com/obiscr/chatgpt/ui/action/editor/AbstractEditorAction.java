package com.obiscr.chatgpt.ui.action.editor;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.NativeFileType;
import com.obiscr.chatgpt.core.SendAction;
import com.obiscr.chatgpt.ui.MainPanel;
import com.obiscr.chatgpt.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static com.obiscr.chatgpt.MyToolWindowFactory.ACTIVE_CONTENT;

/**
 * @author Wuzi
 */
public abstract class AbstractEditorAction extends AnAction {

    protected String text = "";
    protected String key = "";

    public AbstractEditorAction(@NotNull Supplier<String> dynamicText) {
        super(dynamicText);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        doActionPerformed(e);
    }

    public void doActionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        assert editor != null;
        String selectedText = editor.getSelectionModel().getSelectedText();

        // Here is to adapt to the custom prefix
        String prefix = (String) e.getProject().getUserData(CustomAction.ACTIVE_PREFIX);
        if (StringUtil.isNotEmpty(prefix)) {
            key = prefix + ":";
            String customPrompt = (String) e.getProject().getUserData(CustomAction.ACTIVE_PROMPT);
            selectedText = StringUtil.isEmpty(customPrompt) ? selectedText : customPrompt;
            resetUserData(e);
        }

        String apiText = key + "\n" + "<pre><code>" + selectedText + "</code></pre>";

        SendAction sendAction = e.getProject().getService(SendAction.class);
        Object mainPanel = e.getProject().getUserData(ACTIVE_CONTENT);

        String formattedText = apiText.replace("\n", "<br />");
        sendAction.doActionPerformed((MainPanel) mainPanel, formattedText);
    }

    public void resetUserData(@NotNull AnActionEvent e) {
        e.getProject().putUserData(CustomAction.ACTIVE_PREFIX, "");
        e.getProject().putUserData(CustomAction.ACTIVE_PROMPT, "");
        e.getProject().putUserData(CustomAction.ACTIVE_FILE_TYPE, NativeFileType.INSTANCE);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        assert editor != null;
        boolean hasSelection = editor.getSelectionModel().hasSelection();
        e.getPresentation().setEnabledAndVisible(hasSelection);
    }
}
