/*
 * Copyright (c) 2012 Jared González
 * 
 * Permission is hereby granted, free of charge, to any
 * person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of
 * the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package org.nbs.java.ayatana;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import org.java.ayatana.DefaultExtraMenuAction;
import org.openide.awt.DynamicMenuContent;

/**
 * Clase para acciones adiconales para completar la integración con los metodos
 * propopios de netbeans al momento de generar menus dinámicos
 * 
 * @author Jared González
 */
public class NbsExtraMenuAction extends DefaultExtraMenuAction {
	private boolean initializeLazyMenu = false;
	private Method methodDoInitialize;
	private Method methodDynaModel;
	private Field fieldDynaModel;
	
	private void initializeLazyMenu(Class<?> classMenu) throws NoSuchMethodException, NoSuchFieldException {
		methodDoInitialize = classMenu.getDeclaredMethod("doInitialize", new Class<?>[] {});
		if (!methodDoInitialize.isAccessible())
			methodDoInitialize.setAccessible(true);

		fieldDynaModel = classMenu.getDeclaredField("dynaModel");
		if (!fieldDynaModel.isAccessible())
			fieldDynaModel.setAccessible(true);

		Class<?> classDynaModel = fieldDynaModel.getType();
		methodDynaModel = classDynaModel.getMethod("checkSubmenu", new Class<?>[] {JMenu.class});
		if (!methodDynaModel.isAccessible())
			methodDynaModel.setAccessible(true);
	}
	
	@Override
	public void invokeMenu(JFrame frame, JMenuBar menubar, JMenuItem menuitem, boolean selected, boolean shortcut) {
		super.invokeMenu(frame, menubar, menuitem, selected, shortcut);
		if (selected) {
			if ("org.openide.awt.MenuBar$LazyMenu".equals(menuitem.getClass().getName())) {
				try {
					if (!initializeLazyMenu) {
						initializeLazyMenu(menuitem.getClass());
						initializeLazyMenu = true;
					}
					methodDoInitialize.invoke(menuitem, new Object[] {});
					Object objectDynaModel = fieldDynaModel.get(menuitem);
					methodDynaModel.invoke(objectDynaModel, menuitem);
				} catch (Exception e) {
					Logger.getLogger(NbsExtraMenuAction.class.getName())
							.log(Level.WARNING, "Error invoking LazyMenu", e);
				}
			}
			if (menuitem instanceof DynamicMenuContent) {
				((DynamicMenuContent)menuitem).synchMenuPresenters(null);
			}
		}
	}
}
