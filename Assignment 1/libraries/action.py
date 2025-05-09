def move(row, col, action, grid):
    """
    returns the new position after taking an action in the grid environment.
    
    Args:
        row: current row position
        col: current column position
        action: direction to move ('UP', 'DOWN', 'LEFT', 'RIGHT')
        grid: the environment grid
        
    Returns:
        new_row: new row position after taking the action
        new_col: new column position after taking the action
    """
    new_row, new_col = row, col
    
    if action == 'UP':
        new_row = max(row - 1, 0)
    elif action == 'DOWN':
        new_row = min(row + 1, len(grid) - 1)
    elif action == 'LEFT':
        new_col = max(col - 1, 0)
    elif action == 'RIGHT':
        new_col = min(col + 1, len(grid[0]) - 1)
    
    # Check if the new position is a wall
    if grid[new_row][new_col] == 'WALL':
        return row, col  # Stay in the same place
    return new_row, new_col

def side_actions(action):
    """
    returns the possible side actions that might occur due to the stochastic nature of the environment.
    
    Args:
        action: the intended action ('UP', 'DOWN', 'LEFT', 'RIGHT')
        
    Returns:
        list: the two possible side actions that can occur instead of the intended action.
              For UP/DOWN, the side actions are LEFT/RIGHT.
              For LEFT/RIGHT, the side actions are UP/DOWN.
    """
    if action == 'UP' or action == 'DOWN':
        return ['LEFT', 'RIGHT']
    else:  # LEFT or RIGHT
        return ['UP', 'DOWN']