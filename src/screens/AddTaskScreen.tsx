import React, { useState, useEffect } from 'react';
import {
  View,
  StyleSheet,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import {
  Text,
  TextInput,
  Button,
  useTheme,
  Snackbar,
  SegmentedButtons,
  Chip,
} from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';
import { RootStackParamList } from '../navigation/AppNavigator';
import { useAuth } from '../contexts/AuthContext';
import { databaseService } from '../services/database';
import { Priority, Status, Category } from '../models/types';
import { spacing } from '../theme/theme';
import { format } from 'date-fns';

type AddTaskScreenNavigationProp = StackNavigationProp<RootStackParamList, 'AddTask'>;
type AddTaskScreenRouteProp = RouteProp<RootStackParamList, 'AddTask'>;

export default function AddTaskScreen() {
  const theme = useTheme();
  const navigation = useNavigation<AddTaskScreenNavigationProp>();
  const route = useRoute<AddTaskScreenRouteProp>();
  const { user } = useAuth();

  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [category, setCategory] = useState<Category>(Category.Work);
  const [priority, setPriority] = useState<Priority>(Priority.Medium);
  const [status, setStatus] = useState<Status>(Status.Pending);
  const [dueDate, setDueDate] = useState('');
  const [dueTime, setDueTime] = useState('');
  const [taskProgress, setTaskProgress] = useState('0');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSave = async () => {
    if (!title.trim()) {
      setError('Please enter a task title');
      return;
    }

    if (!user) {
      setError('User not found');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const now = Date.now();
      const progress = parseInt(taskProgress) || 0;

      await databaseService.createTask({
        User_ID: user.User_ID,
        Title: title.trim(),
        Description: description.trim() || undefined,
        Category: category,
        Priority: priority,
        DueAt: dueDate || undefined,
        DueTime: dueTime || undefined,
        Status: status,
        TaskProgress: Math.min(100, Math.max(0, progress)),
        CreatedAt: now,
        UpdatedAt: now,
      });

      setSuccess('Task created successfully!');
      setTimeout(() => {
        navigation.goBack();
      }, 1000);
    } catch (err) {
      console.error('Error creating task:', err);
      setError('Failed to create task');
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]}>
      <KeyboardAvoidingView
        behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        style={styles.keyboardView}
      >
        <ScrollView style={styles.scrollView} keyboardShouldPersistTaps="handled">
          <View style={styles.content}>
            <Text variant="headlineSmall" style={styles.sectionTitle}>
              Task Details
            </Text>

            <TextInput
              label="Title *"
              value={title}
              onChangeText={setTitle}
              mode="outlined"
              style={styles.input}
              placeholder="Enter task title"
            />

            <TextInput
              label="Description"
              value={description}
              onChangeText={setDescription}
              mode="outlined"
              multiline
              numberOfLines={4}
              style={styles.input}
              placeholder="Enter task description (optional)"
            />

            <Text variant="titleMedium" style={styles.fieldLabel}>
              Category
            </Text>
            <View style={styles.chipContainer}>
              {Object.values(Category).map((cat) => (
                <Chip
                  key={cat}
                  selected={category === cat}
                  onPress={() => setCategory(cat)}
                  style={styles.chip}
                  mode="outlined"
                >
                  {cat}
                </Chip>
              ))}
            </View>

            <Text variant="titleMedium" style={styles.fieldLabel}>
              Priority
            </Text>
            <SegmentedButtons
              value={priority}
              onValueChange={(value) => setPriority(value as Priority)}
              buttons={[
                {
                  value: Priority.Low,
                  label: '🟢 Low',
                },
                {
                  value: Priority.Medium,
                  label: '🟡 Medium',
                },
                {
                  value: Priority.High,
                  label: '🔴 High',
                },
              ]}
              style={styles.segmentedButtons}
            />

            <Text variant="titleMedium" style={styles.fieldLabel}>
              Status
            </Text>
            <View style={styles.chipContainer}>
              {Object.values(Status).map((stat) => (
                <Chip
                  key={stat}
                  selected={status === stat}
                  onPress={() => setStatus(stat)}
                  style={styles.chip}
                  mode="outlined"
                >
                  {stat}
                </Chip>
              ))}
            </View>

            <Text variant="titleMedium" style={styles.fieldLabel}>
              Due Date & Time
            </Text>
            <View style={styles.dateTimeRow}>
              <TextInput
                label="Date (YYYY-MM-DD)"
                value={dueDate}
                onChangeText={setDueDate}
                mode="outlined"
                style={[styles.input, styles.halfInput]}
                placeholder="2025-01-31"
              />
              <TextInput
                label="Time (HH:MM)"
                value={dueTime}
                onChangeText={setDueTime}
                mode="outlined"
                style={[styles.input, styles.halfInput]}
                placeholder="14:30"
              />
            </View>

            <TextInput
              label="Progress (%)"
              value={taskProgress}
              onChangeText={setTaskProgress}
              mode="outlined"
              keyboardType="numeric"
              style={styles.input}
              placeholder="0"
            />

            <View style={styles.buttonContainer}>
              <Button
                mode="outlined"
                onPress={() => navigation.goBack()}
                style={styles.button}
                disabled={loading}
              >
                Cancel
              </Button>
              <Button
                mode="contained"
                onPress={handleSave}
                style={styles.button}
                loading={loading}
                disabled={loading}
              >
                Save Task
              </Button>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>

      <Snackbar
        visible={!!error}
        onDismiss={() => setError('')}
        duration={3000}
        action={{ label: 'Dismiss', onPress: () => setError('') }}
      >
        {error}
      </Snackbar>

      <Snackbar
        visible={!!success}
        onDismiss={() => setSuccess('')}
        duration={2000}
        style={{ backgroundColor: theme.colors.primary }}
      >
        {success}
      </Snackbar>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  keyboardView: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  content: {
    padding: spacing.lg,
  },
  sectionTitle: {
    marginBottom: spacing.lg,
    fontWeight: 'bold',
  },
  fieldLabel: {
    marginTop: spacing.md,
    marginBottom: spacing.sm,
  },
  input: {
    marginBottom: spacing.md,
  },
  chipContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: spacing.sm,
    marginBottom: spacing.md,
  },
  chip: {
    marginBottom: spacing.sm,
  },
  segmentedButtons: {
    marginBottom: spacing.md,
  },
  dateTimeRow: {
    flexDirection: 'row',
    gap: spacing.md,
    marginBottom: spacing.md,
  },
  halfInput: {
    flex: 1,
  },
  buttonContainer: {
    flexDirection: 'row',
    gap: spacing.md,
    marginTop: spacing.lg,
  },
  button: {
    flex: 1,
  },
});
