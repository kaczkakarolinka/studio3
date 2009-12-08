package com.aptana.scripting.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jruby.anno.JRubyMethod;

import com.aptana.scope.ScopeSelector;

public class MenuElement extends AbstractBundleElement
{
	private static final String SEPARATOR_TEXT = "-";
	
	private MenuElement _parent;
	private List<MenuElement> _children;
	private String _commandName;

	protected BundleElement _owningBundle;

	protected String _scope;
	
	/**
	 * Snippet
	 * 
	 * @param path
	 */
	public MenuElement(String path)
	{
		super(path);
	}

	/**
	 * addMenu
	 * 
	 * @param menu
	 */
	@JRubyMethod(name = "add_menu")
	public void addMenu(MenuElement menu)
	{
		if (menu != null)
		{
			if (this._children == null)
			{
				this._children = new ArrayList<MenuElement>();
			}
			
			// set parent
			menu._parent = this;
			
			// add to our list
			this._children.add(menu);
		}
	}

	/**
	 * cloneByScope
	 * 
	 * @param scope
	 * @return
	 */
	public MenuElement cloneByScope(String scope)
	{
		MenuElement result = null;
		
		// find all menus in the specified scope
		List<MenuElement> matches = new ArrayList<MenuElement>();
		
		for (MenuElement menu : this.getLeafMenus())
		{
			if (menu.matches(scope))
			{
				matches.add(menu);
			}
		}
		
		// collect into one tree
		
		
		return result;
	}
	
	/**
	 * getChildren
	 * 
	 * @return
	 */
	public MenuElement[] getChildren()
	{
		MenuElement[] result = BundleManager.NO_MENUS;
		
		if (this._children != null && this._children.size() > 0)
		{
			result = this._children.toArray(new MenuElement[this._children.size()]);
		}
		
		return result;
	}
	
	/**
	 * getCommand
	 * 
	 * @return
	 */
	public CommandElement getCommand()
	{
		CommandElement result = null;
		
		if (this.isLeafMenu() && this._owningBundle != null)
		{
			result = this._owningBundle.getCommandByName(this._commandName);
		}
		
		return result;
	}
	
	/**
	 * getCommandName
	 * 
	 * @param name
	 * @return
	 */
	@JRubyMethod(name = "command")
	public String getCommandName(String name)
	{
		return this._commandName;
	}
	
	/**
	 * getLeafMenus
	 * 
	 * @return
	 */
	protected MenuElement[] getLeafMenus()
	{
		Stack<MenuElement> stack = new Stack<MenuElement>();
		List<MenuElement> result = new ArrayList<MenuElement>();
		
		// prime stack
		stack.push(this);
		
		while (stack.size() > 0)
		{
			MenuElement menu = stack.pop();
			
			if (menu.isHierarchicalMenu())
			{
				stack.addAll(menu._children);
			}
			else if (menu.isLeafMenu())
			{
				result.add(menu);
			}
			
			// NOTE: we ignore separators
		}
		
		return result.toArray(new MenuElement[result.size()]);
	}
	
	/**
	 * getParent
	 * 
	 * @return
	 */
	public MenuElement getParent()
	{
		return this._parent;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.aptana.scripting.model.AbstractModel#getScopeSelector()
	 */
	public ScopeSelector getScopeSelector()
	{
		ScopeSelector result = null;
		
		if (this._scope != null)
		{
			result = new ScopeSelector(this._scope);
		}
		else
		{
			MenuElement parent = this._parent;
			
			while (parent != null)
			{
				if (parent._scope != null)
				{
					result = new ScopeSelector(parent._scope);
					break;
				}
				else
				{
					parent = parent._parent;
				}
			}
		}
		
		return result;
	}

	/**
	 * hasChildren
	 * 
	 * @return
	 */
	public boolean hasChildren()
	{
		return this._children != null && this._children.size() > 0;
	}
	
	/**
	 * isHierarchical
	 * 
	 * @return
	 */
	public boolean isHierarchicalMenu()
	{
		return this.isSeparator() == false && this.hasChildren();
	}
	
	/**
	 * isLeafMenu
	 * 
	 * @return
	 */
	public boolean isLeafMenu()
	{
		return this.isSeparator() == false && this.hasChildren() == false;
	}
	
	/**
	 * isSeparator
	 * 
	 * @return
	 */
	public boolean isSeparator()
	{
		return this._displayName != null && this._displayName.startsWith(SEPARATOR_TEXT);
	}
	
	/**
	 * setCommandName
	 * 
	 * @param name
	 */
	@JRubyMethod(name = "command=")
	public void setCommandName(String name)
	{
		this.addMenu(new MenuElement(name));
	}
	
	/**
	 * toSource
	 */
	protected void toSource(SourcePrinter printer)
	{
		printer.printWithIndent("menu \"").print(this._displayName).println("\" {").increaseIndent(); //$NON-NLS-1$ //$NON-NLS-2$
		
		printer.printWithIndent("path: ").println(this._path); //$NON-NLS-1$
		printer.printWithIndent("scope: ").println(this.getScopeSelector().toString()); //$NON-NLS-1$
		
		if (this.hasChildren())
		{
			for (MenuElement menu : this._children)
			{
				menu.toSource(printer);
			}
		}
		
		printer.decreaseIndent().printlnWithIndent("}"); //$NON-NLS-1$
	}

	/**
	 * getOwningBundle
	 * 
	 * @return
	 */
	public BundleElement getOwningBundle()
	{
		return this._owningBundle;
	}

	/**
	 * getScope
	 * 
	 * @return
	 */
	@JRubyMethod(name = "scope")
	public String getScope()
	{
		return this._scope;
	}

	/**
	 * setOwningBundle
	 * 
	 * @param bundle
	 */
	void setOwningBundle(BundleElement bundle)
	{
		this._owningBundle = bundle;
	}

	/**
	 * setScope
	 * 
	 * @param scope
	 */
	@JRubyMethod(name = "scope=")
	public void setScope(String scope)
	{
		this._scope = scope;
	}
}
