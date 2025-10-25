import React, { useState, useEffect, useCallback } from 'react';
import { View, StyleSheet, FlatList, RefreshControl } from 'react-native';
import {
  Text,
  FAB,
  Searchbar,
  Chip,
  Card,
  useTheme,
  Badge,
  IconButton,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation, useFocusEffect } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { useAuth } from '../contexts/AuthContext';
import { databaseService } from '../services/database';
import { Task, Status, Priority } from '../models/types';
import { spacing, priorityColors, statusColors } from '../theme/theme';
import { format } from 'date-fns';

type TaskListScreenNavigationProp = StackNavigationProp<RootStackParamList>;

export default function TaskListScreen() {
  const theme = useTheme();
  const navigation = useNavigation<TaskListScreenNavigationProp>();
  const { user } = useAuth();

  const [tasks, setTasks] = useState<Task[]>([]);
  const [filteredTasks, setFilteredTasks] = useState<Task[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [filter, setFilter] = useState<'all' | 'today' | 'pending' | 'completed'>('all');
  const [refreshing, setRefreshing] = useState(false);
  const [loading, setLoading] = useState(true);

  useFocusEffect(
    useCallback(() => {
      loadTasks();
    }, [user])
  );

  const loadTasks = async () => {
    if (!user) return;

    try {
      const allTasks = await databaseService.getTasksByUser(user.User_ID);
      setTasks(allTasks);
      applyFilters(allTasks, filter, searchQuery);
    } catch (error) {
      console.error('Error loading tasks:', error);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  const applyFilters = (taskList: Task[], currentFilter: string, query: string) => {
    let filtered = [...taskList];

    // Apply status filter
    if (currentFilter === 'today') {
      const today = new Date();
      filtered = filtered.filter((task) => {
        if (!task.DueAt) return false;
        const dueDate = new Date(task.DueAt);
        return (
          dueDate.getDate() === today.getDate() &&
          dueDate.getMonth() === today.getMonth() &&
          dueDate.getFullYear() === today.getFullYear()
        );
      });
    } else if (currentFilter === 'pending') {
      filtered = filtered.filter((task) => task.Status === Status.Pending);
    } else if (currentFilter === 'completed') {
      filtered = filtered.filter((task) => task.Status === Status.Completed);
    }

    // Apply search filter
    if (query.trim()) {
      const lowerQuery = query.toLowerCase();
      filtered = filtered.filter(
        (task) =>
          task.Title.toLowerCase().includes(lowerQuery) ||
          (task.Description && task.Description.toLowerCase().includes(lowerQuery))
      );
    }

    setFilteredTasks(filtered);
  };

  const handleSearch = (query: string) => {
    setSearchQuery(query);
    applyFilters(tasks, filter, query);
  };

  const handleFilterChange = (newFilter: typeof filter) => {
    setFilter(newFilter);
    applyFilters(tasks, newFilter, searchQuery);
  };

  const handleRefresh = () => {
    setRefreshing(true);
    loadTasks();
  };

  const getPriorityIcon = (priority: Priority) => {
    switch (priority) {
      case Priority.High:
        return '🔴';
      case Priority.Medium:
        return '🟡';
      case Priority.Low:
        return '🟢';
    }
  };

  const getStatusIcon = (status: Status) => {
    switch (status) {
      case Status.Completed:
        return '✅';
      case Status.Pending:
        return '⏳';
      case Status.InProgress:
        return '🔄';
      case Status.Cancelled:
        return '❌';
      case Status.OverDue:
        return '🔴';
    }
  };

  const renderTask = ({ item }: { item: Task }) => (
    <Card
      style={styles.taskCard}
      onPress={() => navigation.navigate('TaskDetail', { taskId: item.Task_ID })}
    >
      <Card.Content>
        <View style={styles.taskHeader}>
          <View style={styles.taskTitle}>
            <Text variant="titleMedium" numberOfLines={1}>
              {getPriorityIcon(item.Priority)} {item.Title}
            </Text>
          </View>
          <Badge
            style={{
              backgroundColor: statusColors[item.Status],
            }}
          >
            {getStatusIcon(item.Status)}
          </Badge>
        </View>

        {item.Description && (
          <Text
            variant="bodyMedium"
            numberOfLines={2}
            style={[styles.description, { color: theme.colors.onSurfaceVariant }]}
          >
            {item.Description}
          </Text>
        )}

        <View style={styles.taskFooter}>
          <Chip
            icon="folder"
            compact
            style={styles.chip}
          >
            {item.Category}
          </Chip>
          {item.DueAt && (
            <Chip
              icon="calendar"
              compact
              style={styles.chip}
            >
              {format(new Date(item.DueAt), 'MMM dd')}
            </Chip>
          )}
          {item.TaskProgress > 0 && (
            <Chip
              icon="progress-check"
              compact
              style={styles.chip}
            >
              {item.TaskProgress}%
            </Chip>
          )}
        </View>
      </Card.Content>
    </Card>
  );

  const getFilterCounts = () => {
    return {
      all: tasks.length,
      today: tasks.filter((t) => {
        if (!t.DueAt) return false;
        const dueDate = new Date(t.DueAt);
        const today = new Date();
        return (
          dueDate.getDate() === today.getDate() &&
          dueDate.getMonth() === today.getMonth() &&
          dueDate.getFullYear() === today.getFullYear()
        );
      }).length,
      pending: tasks.filter((t) => t.Status === Status.Pending).length,
      completed: tasks.filter((t) => t.Status === Status.Completed).length,
    };
  };

  const counts = getFilterCounts();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]} edges={['bottom']}>
      <View style={styles.content}>
        <Searchbar
          placeholder="Search tasks..."
          onChangeText={handleSearch}
          value={searchQuery}
          style={styles.searchBar}
        />

        <View style={styles.filters}>
          <Chip
            selected={filter === 'all'}
            onPress={() => handleFilterChange('all')}
            style={styles.filterChip}
          >
            All ({counts.all})
          </Chip>
          <Chip
            selected={filter === 'today'}
            onPress={() => handleFilterChange('today')}
            style={styles.filterChip}
          >
            Today ({counts.today})
          </Chip>
          <Chip
            selected={filter === 'pending'}
            onPress={() => handleFilterChange('pending')}
            style={styles.filterChip}
          >
            Pending ({counts.pending})
          </Chip>
          <Chip
            selected={filter === 'completed'}
            onPress={() => handleFilterChange('completed')}
            style={styles.filterChip}
          >
            Done ({counts.completed})
          </Chip>
        </View>

        {filteredTasks.length === 0 ? (
          <View style={styles.emptyState}>
            <Text variant="headlineSmall" style={{ color: theme.colors.onSurfaceVariant }}>
              {loading ? 'Loading tasks...' : '📋 No tasks found'}
            </Text>
            {!loading && (
              <Text variant="bodyMedium" style={{ marginTop: spacing.sm, color: theme.colors.onSurfaceVariant }}>
                {searchQuery
                  ? 'Try adjusting your search'
                  : "Tap the + button to create your first task"}
              </Text>
            )}
          </View>
        ) : (
          <FlatList
            data={filteredTasks}
            renderItem={renderTask}
            keyExtractor={(item) => item.Task_ID.toString()}
            contentContainerStyle={styles.listContent}
            refreshControl={
              <RefreshControl refreshing={refreshing} onRefresh={handleRefresh} />
            }
          />
        )}

        <FAB
          icon="plus"
          style={[styles.fab, { backgroundColor: theme.colors.primary }]}
          onPress={() => navigation.navigate('AddTask')}
        />
      </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  content: {
    flex: 1,
  },
  searchBar: {
    margin: spacing.md,
    elevation: 2,
  },
  filters: {
    flexDirection: 'row',
    paddingHorizontal: spacing.md,
    paddingBottom: spacing.md,
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  filterChip: {
    marginRight: spacing.sm,
  },
  listContent: {
    padding: spacing.md,
  },
  taskCard: {
    marginBottom: spacing.md,
    elevation: 2,
  },
  taskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  taskTitle: {
    flex: 1,
    marginRight: spacing.sm,
  },
  description: {
    marginBottom: spacing.md,
  },
  taskFooter: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
  },
  chip: {
    marginRight: spacing.sm,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: spacing.xl,
  },
  fab: {
    position: 'absolute',
    margin: spacing.md,
    right: 0,
    bottom: 0,
  },
});
