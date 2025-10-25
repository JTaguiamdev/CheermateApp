import React from 'react';
import { View, StyleSheet, ScrollView } from 'react-native';
import { Text, Avatar, Card, List, useTheme } from 'react-native-paper';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useAuth } from '../contexts/AuthContext';
import { spacing } from '../theme/theme';

export default function ProfileScreen() {
  const theme = useTheme();
  const { user } = useAuth();

  return (
    <SafeAreaView style={[styles.container, { backgroundColor: theme.colors.background }]} edges={['bottom']}>
      <ScrollView style={styles.content}>
        <Card style={styles.profileCard}>
          <Card.Content style={styles.profileHeader}>
            <Avatar.Text
              size={80}
              label={user?.Username.substring(0, 2).toUpperCase() || 'U'}
              style={{ backgroundColor: theme.colors.primary }}
            />
            <Text variant="headlineSmall" style={styles.username}>
              {user?.Username}
            </Text>
            <Text variant="bodyMedium" style={{ color: theme.colors.onSurfaceVariant }}>
              {user?.Email}
            </Text>
            {user?.FirstName && user?.LastName && (
              <Text variant="bodyLarge" style={styles.fullName}>
                {user.FirstName} {user.LastName}
              </Text>
            )}
          </Card.Content>
        </Card>

        <Card style={styles.statsCard}>
          <Card.Content>
            <Text variant="titleMedium" style={styles.sectionTitle}>
              Statistics
            </Text>
            <View style={styles.statsGrid}>
              <View style={styles.statItem}>
                <Text variant="displaySmall" style={{ color: theme.colors.primary }}>
                  0
                </Text>
                <Text variant="bodyMedium">Tasks</Text>
              </View>
              <View style={styles.statItem}>
                <Text variant="displaySmall" style={{ color: theme.colors.primary }}>
                  0
                </Text>
                <Text variant="bodyMedium">Completed</Text>
              </View>
              <View style={styles.statItem}>
                <Text variant="displaySmall" style={{ color: theme.colors.primary }}>
                  0%
                </Text>
                <Text variant="bodyMedium">Success Rate</Text>
              </View>
            </View>
          </Card.Content>
        </Card>

        <List.Section>
          <List.Subheader>Account Information</List.Subheader>
          <List.Item
            title="Edit Profile"
            left={(props) => <List.Icon {...props} icon="account-edit" />}
            right={(props) => <List.Icon {...props} icon="chevron-right" />}
          />
          <List.Item
            title="Security"
            left={(props) => <List.Icon {...props} icon="shield-account" />}
            right={(props) => <List.Icon {...props} icon="chevron-right" />}
          />
          <List.Item
            title="Personality"
            description="Choose your assistant personality"
            left={(props) => <List.Icon {...props} icon="emoticon" />}
            right={(props) => <List.Icon {...props} icon="chevron-right" />}
          />
        </List.Section>
      </ScrollView>
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
  profileCard: {
    margin: spacing.md,
    elevation: 2,
  },
  profileHeader: {
    alignItems: 'center',
    paddingVertical: spacing.lg,
  },
  username: {
    marginTop: spacing.md,
    fontWeight: 'bold',
  },
  fullName: {
    marginTop: spacing.sm,
  },
  statsCard: {
    margin: spacing.md,
    elevation: 2,
  },
  sectionTitle: {
    marginBottom: spacing.md,
  },
  statsGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  statItem: {
    alignItems: 'center',
  },
});
