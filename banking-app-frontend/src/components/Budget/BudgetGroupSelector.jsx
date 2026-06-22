import { useState, useEffect } from "react";
import { getMyBudgetGroups, deleteBudgetGroup } from "../../services/budgetApi";

function BudgetGroupSelector({ activeGroupId, onSelectGroup, onCreateNewGroup, onGroupDeleted, onGroupCreated }) {
    const [groups, setGroups] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchGroups();
    }, []);

    useEffect(() => {
        if (onGroupCreated) {
            fetchGroups();
        }
    }, [onGroupCreated]);

    const fetchGroups = async () => {
        try {
            const response = await getMyBudgetGroups();
            setGroups(response.data);

            if (!activeGroupId && response.data.length > 0) {
                onSelectGroup(response.data[0].id);
            }
        } catch (error) {
            console.error("Failed to fetch budget groups", error);
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteGroup = async (groupId) => {
        if (!window.confirm("Are you sure you want to delete this group? This will delete all budgets and expenses in this group.")) {
            return;
        }

        try {
            await deleteBudgetGroup(groupId);
            setGroups(prev => prev.filter(group => group.id !== groupId));

            // If the deleted group was the active one, select the first available group
            if (activeGroupId === groupId && groups.length > 1) {
                const newActiveGroup = groups.find(group => group.id !== groupId);
                onSelectGroup(newActiveGroup.id);
            } else if (activeGroupId === groupId) {
                onSelectGroup(null); // No groups left
            }

            // Call the callback on successful deletion
            if (onGroupDeleted) {
                onGroupDeleted();
            }

        } catch (error) {
            console.error("Failed to delete budget group", error);
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return <p className="text-gray-400 text-xs">Loading groups...</p>;
    }

    return (
        <div className="flex items-center gap-2 mb-4 overflow-x-auto pb-2 pt-2">
            {groups.map((group) => (
                <div key={group.id} className="relative group">
                    <button
                        onClick={() => onSelectGroup(group.id)}
                        className={
                            activeGroupId === group.id
                                ? "bg-gradient-to-r from-blue-500 to-purple-500 text-white text-sm font-medium px-4 py-2 rounded-xl whitespace-nowrap transition-all"
                                : "bg-[#fefefe] border border-[#e8e8e3] text-gray-600 text-sm font-medium px-4 py-2 rounded-xl whitespace-nowrap hover:bg-[#f0f0eb] transition-all"
                        }
                    >
                        {group.name}
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteGroup(group.id);
                        }}
                        className="
                            absolute -top-1 -right-1
                            w-5 h-5 rounded-full bg-white shadow
                            flex items-center justify-center
                            text-gray-500 text-xs
                            opacity-0 scale-75
                            group-hover:opacity-100 group-hover:scale-100
                            hover:bg-red-500 hover:text-white
                            transition-all"
                    >
                        ✕
                    </button>
                </div>
            ))}
            <button
                onClick={onCreateNewGroup}
                className="bg-[#fefefe] border border-dashed border-[#d1d5db] text-gray-400 text-sm font-medium px-4 py-2 rounded-xl whitespace-nowrap hover:border-blue-400 hover:text-blue-500 transition-all"
            >
                + New Budget Group
            </button>
        </div>
    );
}

export default BudgetGroupSelector;