import tkinter as tk
from tkinter import ttk, messagebox, simpledialog
import json
import os
from datetime import datetime

class TaskManager:
    def __init__(self, filename="tasks.json"):
        self.filename = filename
        self.tasks = self.load_tasks()
        self.task_id_counter = len(self.tasks) + 1

    def load_tasks(self):
        if os.path.exists(self.filename):
            with open(self.filename, 'r') as f:
                return json.load(f)
        return []

    def save_tasks(self):
        with open(self.filename, 'w') as f:
            json.dump(self.tasks, f, indent=2)

    def add_task(self, title, priority="Low", deadline=""):
        task = {
            "id": self.task_id_counter,
            "title": title,
            "priority": priority,
            "deadline": deadline,
            "completed": False,
            "created_at": datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        }
        self.tasks.append(task)
        self.task_id_counter += 1
        self.save_tasks()
        return task

    def view_tasks(self):
        return self.tasks

    def edit_task(self, task_id, new_title=None, new_priority=None, new_deadline=None):
        for task in self.tasks:
            if task["id"] == task_id:
                if new_title:
                    task["title"] = new_title
                if new_priority:
                    task["priority"] = new_priority
                if new_deadline:
                    task["deadline"] = new_deadline
                self.save_tasks()
                return True
        return False

    def delete_task(self, task_id):
        self.tasks = [t for t in self.tasks if t["id"] != task_id]
        self.save_tasks()

    def mark_complete(self, task_id):
        for task in self.tasks:
            if task["id"] == task_id:
                task["completed"] = not task["completed"]
                self.save_tasks()
                return True
        return False

    def search_tasks(self, keyword):
        return [t for t in self.tasks if keyword.lower() in t["title"].lower()]


class TaskManagerGUI:
    def __init__(self, root):
        self.root = root
        self.root.title("Task Management System")
        self.root.geometry("900x700")
        self.manager = TaskManager()
        self.selected_task_id = None
        
        self.setup_ui()
        self.refresh_tasks()

    def setup_ui(self):
        # Header
        header = ttk.Frame(self.root, padding="10")
        header.pack(fill=tk.X)
        ttk.Label(header, text="Task Management System", font=("Arial", 18, "bold")).pack()

        # Input Frame
        input_frame = ttk.LabelFrame(self.root, text="Add New Task", padding="10")
        input_frame.pack(fill=tk.X, padx=10, pady=5)

        ttk.Label(input_frame, text="Title:").grid(row=0, column=0, sticky="w", padx=5)
        self.title_entry = ttk.Entry(input_frame, width=35)
        self.title_entry.grid(row=0, column=1, padx=5)

        ttk.Label(input_frame, text="Priority:").grid(row=0, column=2, sticky="w", padx=5)
        self.priority_var = tk.StringVar(value="Low")
        priority_combo = ttk.Combobox(input_frame, textvariable=self.priority_var, 
                                      values=["Low", "Medium", "High"], width=12, state="readonly")
        priority_combo.grid(row=0, column=3, padx=5)

        ttk.Button(input_frame, text="Add Task", command=self.add_task).grid(row=0, column=4, padx=5)

        ttk.Label(input_frame, text="Deadline:").grid(row=1, column=0, sticky="w", padx=5)
        self.deadline_entry = ttk.Entry(input_frame, width=35)
        self.deadline_entry.grid(row=1, column=1, padx=5)
        ttk.Label(input_frame, text="(YYYY-MM-DD)").grid(row=1, column=2, sticky="w")

        # Search Frame
        search_frame = ttk.Frame(self.root, padding="10")
        search_frame.pack(fill=tk.X)

        ttk.Label(search_frame, text="Search:").pack(side=tk.LEFT, padx=5)
        self.search_entry = ttk.Entry(search_frame, width=35)
        self.search_entry.pack(side=tk.LEFT, padx=5)
        ttk.Button(search_frame, text="Search", command=self.search_tasks).pack(side=tk.LEFT, padx=5)
        ttk.Button(search_frame, text="Show All", command=self.refresh_tasks).pack(side=tk.LEFT, padx=5)

        # Tasks Frame
        tasks_frame = ttk.LabelFrame(self.root, text="Tasks", padding="10")
        tasks_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=5)

        # Treeview
        self.tree = ttk.Treeview(tasks_frame, columns=("ID", "Title", "Priority", "Deadline", "Status", "Created"), 
                                 height=15, show="headings")
        self.tree.column("ID", width=50)
        self.tree.column("Title", width=280)
        self.tree.column("Priority", width=80)
        self.tree.column("Deadline", width=120)
        self.tree.column("Status", width=100)
        self.tree.column("Created", width=140)

        self.tree.heading("ID", text="ID")
        self.tree.heading("Title", text="Title")
        self.tree.heading("Priority", text="Priority")
        self.tree.heading("Deadline", text="Deadline")
        self.tree.heading("Status", text="Status")
        self.tree.heading("Created", text="Created")

        scrollbar = ttk.Scrollbar(tasks_frame, orient=tk.VERTICAL, command=self.tree.yview)
        self.tree.configure(yscroll=scrollbar.set)

        self.tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        
        self.tree.bind('<<TreeviewSelect>>', self.on_task_select)

        # Buttons Frame
        button_frame = ttk.Frame(self.root, padding="10")
        button_frame.pack(fill=tk.X)

        ttk.Button(button_frame, text="Mark Complete", command=self.mark_complete).pack(side=tk.LEFT, padx=5)
        ttk.Button(button_frame, text="Edit", command=self.edit_task).pack(side=tk.LEFT, padx=5)
        ttk.Button(button_frame, text="Delete", command=self.delete_task).pack(side=tk.LEFT, padx=5)

    def add_task(self):
        title = self.title_entry.get().strip()
        if not title:
            messagebox.showwarning("Input Error", "Please enter a task title.")
            return
        
        priority = self.priority_var.get()
        deadline = self.deadline_entry.get().strip()
        
        self.manager.add_task(title, priority, deadline)
        self.title_entry.delete(0, tk.END)
        self.deadline_entry.delete(0, tk.END)
        self.refresh_tasks()
        messagebox.showinfo("Success", f"Task '{title}' added successfully!")

    def refresh_tasks(self):
        for item in self.tree.get_children():
            self.tree.delete(item)
        
        for task in self.manager.view_tasks():
            status = "✓ Complete" if task["completed"] else "○ Pending"
            self.tree.insert("", "end", values=(
                task["id"],
                task["title"],
                task["priority"],
                task["deadline"] or "N/A",
                status,
                task["created_at"]
            ))

    def on_task_select(self, event):
        selected = self.tree.selection()
        if selected:
            self.selected_task_id = int(self.tree.item(selected[0])["values"][0])

    def mark_complete(self):
        if self.selected_task_id is None:
            messagebox.showwarning("Selection Error", "Please select a task.")
            return
        
        self.manager.mark_complete(self.selected_task_id)
        self.refresh_tasks()
        messagebox.showinfo("Success", "Task status updated!")

    def edit_task(self):
        if self.selected_task_id is None:
            messagebox.showwarning("Selection Error", "Please select a task.")
            return
        
        task = next((t for t in self.manager.tasks if t["id"] == self.selected_task_id), None)
        
        if not task:
            return
        
        new_title = simpledialog.askstring("Edit Title", "New title:", initialvalue=task["title"])
        if new_title is None:
            return
        
        new_priority = simpledialog.askstring("Edit Priority", "New priority:", initialvalue=task["priority"])
        new_deadline = simpledialog.askstring("Edit Deadline", "New deadline:", initialvalue=task["deadline"])
        
        self.manager.edit_task(self.selected_task_id, new_title, new_priority, new_deadline)
        self.refresh_tasks()
        messagebox.showinfo("Success", "Task updated!")

    def delete_task(self):
        if self.selected_task_id is None:
            messagebox.showwarning("Selection Error", "Please select a task.")
            return
        
        if messagebox.askyesno("Confirm", "Are you sure you want to delete this task?"):
            self.manager.delete_task(self.selected_task_id)
            self.refresh_tasks()
            messagebox.showinfo("Success", "Task deleted!")

    def search_tasks(self):
        keyword = self.search_entry.get().strip()
        if not keyword:
            messagebox.showwarning("Input Error", "Please enter a search keyword.")
            return
        
        for item in self.tree.get_children():
            self.tree.delete(item)
        
        results = self.manager.search_tasks(keyword)
        for task in results:
            status = "✓ Complete" if task["completed"] else "○ Pending"
            self.tree.insert("", "end", values=(
                task["id"],
                task["title"],
                task["priority"],
                task["deadline"] or "N/A",
                status,
                task["created_at"]
            ))
        
        if not results:
            messagebox.showinfo("Search", f"No tasks found matching '{keyword}'")


if __name__ == "__main__":
    root = tk.Tk()
    gui = TaskManagerGUI(root)
    root.mainloop()