import React, { useState, useEffect } from 'react';
import { View, StyleSheet, ScrollView, Alert } from 'react-native';
import {
  Text,
  Card,
  Button,
  IconButton,
  Chip,
  useTheme,
  Divider,
  ProgressBar,
  Dialog,
  Portal,
  TextInput,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { useAuth } from '../contexts/AuthContext';
import { databaseService } from '../services/database';
import { Task, SubTask, Priority, Status } from '../models/types';
import { spacing, priorityColors, statusColors } from '../theme/theme';
import { format } from 'date-fns';

type TaskDetailScreenNavigationProp = StackNavigationProp<RootStackParamList, 'TaskDetail'>;
type TaskDetailScreenRouteProp = RouteProp<RootStackParamList, 'TaskDetail'>;

export default function TaskDetailScreen() {
  const theme = useTheme();
  const navigation = useNavigation<TaskDetailScreenNavigationProp>();
  const route = useRoute<TaskDetailScreenRouteProp>();
  const { user } = useAuth();

  const [task, setTask] = useState<Task | null>(null);
  const [subTasks, setSubTasks] = useState<SubTask[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddSubTask, setShowAddSubTask] = useState(false);
  const [newSubTaskTitle, setNewSubTaskTitle] = useState('');

  useEffect(() => {
    loadTask();
  }, [route.params.taskId]);

  const loadTask = async () => {
    if (!user) return;

    try {
      const taskData = await databaseService.getTaskById(route.params.taskId, user.User_ID);
      if (taskData) {
        setTask(taskData);
        const subTaskData = await databaseService.getSubTasksByTask(taskData.Task_ID);
        setSubTasks(subTaskData);
      }
    } catch (error) {
      console.error('Error loading task:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = () => {
    Alert.alert(
      'Delete Task',
      'Are you sure you want to delete this task?',
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            if (task && user) {
              await databaseService.deleteTask(task.Task_ID, user.User_ID);
              navigation.goBack();
            }
          },
        },
      ]
    );
  };

  const handleMarkAsDone = async () => {
    if (!task || !user) return;

    try {
      await databaseService.updateTask(task.Task_ID, user.User_ID, {
        Status: Status.Completed,
        TaskProgress: 100,
      });
      loadTask();
    } catch (error) {
      console.error('Error updating task:', error);
    }
  };

  const handleAddSubTask = async () => {
    if (!newSubTaskTitle.trim() || !task || !user) return;

    try {
      await databaseService.createSubTask({
        Task_ID: task.Task_ID,
        User_ID: user.User_ID,
        Title: newSubTaskTitle.trim(),
        IsCompleted: false,
        CreatedAt: Date.now(),
      });
      setNewSubTaskTitle('');
      setShowAddSubTask(false);
      loadTask();
    } catch (error) {
      console.error('Error adding subtask:', error);
    }
  };

  const handleToggleSubTask = async (subTask: SubTask) => {
    try {
      await databaseService.updateSubTask(subTask.SubTask_ID, {
        IsCompleted: !subTask.IsCompleted,
      });
      loadTask();
    } catch (error) {
      console.error('Error toggling subtask:', error);
    }
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

  if (loading) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.loadingContainer}>
          <Text>Loading...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (!task) {
    return (
      <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
        <View style={styles.loadingContainer}>
          <Text>Task not found</Text>
        </View>
      </SafeAreaView>
    );
  }

  const completedSubTasks = subTasks.filter(st => st.IsCompleted).length;
  const totalSubTasks = subTasks.length;

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <ScrollView style={styles.scrollView}>
        <Card style={styles.card}>
          <Card.Content>
            <View style={styles.header}>
              <View style={styles.titleRow}>
                <Text variant="headlineSmall" style={styles.title}>
                  {getPriorityIcon(task.Priority)} {task.Title}
                </Text>
              </View>
              <View style={styles.actions}>
                <IconButton
                  icon="pencil"
                  size={24}
                  onPress={() => {
                    // Navigate to edit screen (would need to implement)
                  }}
                />
                <IconButton
                  icon="delete"
                  size={24}
                  onPress={handleDelete}
                  iconColor={theme.colors.error}
                />
              </View>
            </View>

            <View style={styles.badges}>
              <Chip
                icon="flag"
                style={{ backgroundColor: priorityColors[task.Priority] }}
                textStyle={{ color: '#FFF' }}
              >
                {task.Priority}
              </Chip>
              <Chip
                style={{ backgroundColor: statusColors[task.Status] }}
                textStyle={{ color: '#FFF' }}
              >
                {getStatusIcon(task.Status)} {task.Status}
              </Chip>
              <Chip icon="folder">{task.Category}</Chip>
            </View>

            {task.Description && (
              <View style={styles.section}>
                <Text variant="titleMedium" style={styles.sectionTitle}>
                  Description
                </Text>
                <Text variant="bodyLarge">{task.Description}</Text>
              </View>
            )}

            {task.DueAt && (
              <View style={styles.section}>
                <Text variant="titleMedium" style={styles.sectionTitle}>
                  Due Date
                </Text>
                <Text variant="bodyLarge">
                  📅 {format(new Date(task.DueAt), 'MMMM dd, yyyy')}
                  {task.DueTime && ` at ${task.DueTime}`}
                </Text>
              </View>
            )}

            <View style={styles.section}>
              <Text variant="titleMedium" style={styles.sectionTitle}>
                Progress
              </Text>
              <View style={styles.progressContainer}>
                <ProgressBar
                  progress={task.TaskProgress / 100}
                  color={theme.colors.primary}
                  style={styles.progressBar}
                />
                <Text variant="bodyMedium">{task.TaskProgress}%</Text>
              </View>
            </View>
          </Card.Content>
        </Card>

        <Card style={styles.card}>
          <Card.Content>
            <View style={styles.subTaskHeader}>
              <Text variant="titleLarge">
                Subtasks ({completedSubTasks}/{totalSubTasks})
              </Text>
              <IconButton
                icon="plus"
                size={24}
                onPress={() => setShowAddSubTask(true)}
              />
            </View>

            {subTasks.length === 0 ? (
              <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
                No subtasks yet. Add one to break down this task!
              </Text>
            ) : (
              subTasks.map((subTask) => (
                <View key={subTask.SubTask_ID} style={styles.subTaskItem}>
                  <IconButton
                    icon={subTask.IsCompleted ? 'checkbox-marked' : 'checkbox-blank-outline'}
                    size={24}
                    onPress={() => handleToggleSubTask(subTask)}
                    iconColor={subTask.IsCompleted ? theme.colors.primary : undefined}
                  />
                  <Text
                    variant="bodyLarge"
                    style={[
                      styles.subTaskTitle,
                      subTask.IsCompleted && styles.subTaskCompleted,
                    ]}
                  >
                    {subTask.Title}
                  </Text>
                </View>
              ))
            )}
          </Card.Content>
        </Card>

        {task.Status !== Status.Completed && (
          <View style={styles.buttonContainer}>
            <Button
              mode="contained"
              icon="check"
              onPress={handleMarkAsDone}
              style={styles.doneButton}
            >
              Mark as Done
            </Button>
          </View>
        )}

        <View style={styles.metadata}>
          <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant }}>
            Created: {format(new Date(task.CreatedAt), 'MMM dd, yyyy HH:mm')}
          </Text>
          <Text variant="bodySmall" style={{ color: theme.colors.onSurfaceVariant }}>
            Updated: {format(new Date(task.UpdatedAt), 'MMM dd, yyyy HH:mm')}
          </Text>
        </View>
      </ScrollView>

      <Portal>
        <Dialog visible={showAddSubTask} onDismiss={() => setShowAddSubTask(false)}>
          <Dialog.Title>Add Subtask</Dialog.Title>
          <Dialog.Content>
            <TextInput
              label="Subtask Title"
              value={newSubTaskTitle}
              onChangeText={setNewSubTaskTitle}
              mode="outlined"
              autoFocus
            />
          </Dialog.Content>
          <Dialog.Actions>
            <Button onPress={() => setShowAddSubTask(false)}>Cancel</Button>
            <Button onPress={handleAddSubTask}>Add</Button>
          </Dialog.Actions>
        </Dialog>
      </Portal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  card: {
    margin: spacing.md,
    elevation: 2,
  },
  header: {
    marginBottom: spacing.md,
  },
  titleRow: {
    flex: 1,
  },
  title: {
    fontWeight: 'bold',
    marginBottom: spacing.sm,
  },
  actions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
  },
  badges: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  section: {
    marginTop: spacing.lg,
  },
  sectionTitle: {
    marginBottom: spacing.sm,
    fontWeight: 'bold',
  },
  progressContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: spacing.md,
  },
  progressBar: {
    flex: 1,
    height: 8,
    borderRadius: 4,
  },
  subTaskHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: spacing.md,
  },
  subTaskItem: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: spacing.sm,
  },
  subTaskTitle: {
    flex: 1,
  },
  subTaskCompleted: {
    textDecorationLine: 'line-through',
    opacity: 0.6,
  },
  buttonContainer: {
    padding: spacing.md,
  },
  doneButton: {
    paddingVertical: spacing.sm,
  },
  metadata: {
    padding: spacing.md,
    gap: spacing.sm,
  },
});
