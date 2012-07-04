package jeshua.rl.uct.demo;

import java.util.ArrayList;
import java.util.Random;

public class Maze {
	// Cell properties
	public static final int N = 1;// wall to north
	public static final int S = 2;// wall to south
	public static final int E = 4;// wall to east
	public static final int W = 8;// wall to west
	public static final int G = 16;// goal location
	public static final int B = 32;// begin location

	private int height, width;
	private int[][] maze;
	private int startY, startX, goalY, goalX;

	public Maze(int[][] maze) {
		this.maze = maze;
		this.height = maze.length;
		this.width = maze[0].length;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				if ((maze[i][j] & B) > 0) {
					startY = i;
					startX = j;
				}
				if ((maze[i][j] & G) > 0) {
					goalY = i;
					goalX = j;
				}
			}
		}
	}

	public int goalY() {
		return goalY;
	}

	public int goalX() {
		return goalX;
	}

	public int startY() {
		return startY;
	}

	public int startX() {
		return startX;
	}

	public int width() {
		return width;
	}

	public int height() {
		return height;
	}

	public void setCell(int x, int y, int v) {
		maze[y][x] = v;
		if (v == G) {
			goalX = x;
			goalY = y;
		}

	}

	public boolean isGoal(int x, int y) {
		return (y == goalY && x == goalX);
	}

	// actions are NSEW = {0,1,2,3}
	public boolean legalMove(int x, int y, int action) {
		if (x >= width || x < 0 || y >= height || y < 0)
			return false;
		else {

			switch (action) {
			case 0:
				if (y > 0 && ((maze[y][x] & N) == 0)
						&& ((maze[y - 1][x] & S) == 0)) {
					return true;
				}
				break;
			case 1:
				if ((y < height - 1) && ((maze[y][x] & S) == 0)
						&& ((maze[y + 1][x] & N) == 0)) {
					return true;
				}
				break;
			case 2:
				if ((x < width - 1) && ((maze[y][x] & E) == 0)
						&& ((maze[y][x + 1] & W) == 0)) {
					return true;
				}
				break;
			case 3:
				if (x > 0 && ((maze[y][x] & W) == 0)
						&& ((maze[y][x - 1] & E) == 0)) {
					return true;
				}
				break;
			}
			return false;
		}
	}

	// ==================================================
	// print representation of maze to stdout

	public void print(int agx, int agy)// print with agent at x,y
	{
		int[][] maze = this.maze;
		String str;

		for (int x = 0; x < maze[0].length; x++)
			System.out.print(" _");
		System.out.println();
		for (int y = 0; y < maze.length; y++) {
			System.out.print("|");
			for (int x = 0; x < maze[y].length; x++) {
				str = "";
				if (y == agy && x == agx)
					str += "X";
				else if (isGoal(x, y))
					str += "G";
				else if (!legalMove(x, y, 1))
					str += "_";
				else
					str += " ";
				if (!legalMove(x, y, 2))
					str += "|";
				else
					str += " ";

				System.out.print(str);
			}
			System.out.print("\n");
		}
		System.out.println("");
	}

	// ==================================================
	// Making random mazes

	private static class Wall {
		public int x;
		public int y;
		public int dir;

		public Wall(int x, int y, int dir) {
			this.x = x;
			this.y = y;
			this.dir = dir;
		}
	}

	private static void addWalls(ArrayList<Wall> walls, int x, int y) {
		walls.add(new Wall(x, y, N));
		walls.add(new Wall(x, y, S));
		walls.add(new Wall(x, y, E));
		walls.add(new Wall(x, y, W));
	}

	public static int[][] randomMaze(int width, int height, Random rand) {
		int[][] floors = new int[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < height; j++)
				floors[i][j] = 1;

		int[][] maze_data = new int[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < height; j++)
				maze_data[i][j] = N | S | E | W;

		ArrayList<Wall> walls = new ArrayList<Wall>();
		addWalls(walls, rand.nextInt(height), rand.nextInt(width));
		Wall p;
		while (walls.size() > 0) {
			int ind = rand.nextInt(walls.size());
			p = walls.get(ind);
			walls.remove(ind);
			if (p.dir == N && p.y - 1 >= 0 && floors[p.y - 1][p.x] != 0) {
				floors[p.y - 1][p.x] = 0;
				maze_data[p.y][p.x] &= ~N;
				maze_data[p.y - 1][p.x] &= ~S;
				walls.add(new Wall(p.x, p.y - 1, N));
				walls.add(new Wall(p.x, p.y - 1, E));
				walls.add(new Wall(p.x, p.y - 1, W));
			}
			if (p.dir == S && p.y + 1 < height && floors[p.y + 1][p.x] != 0) {
				floors[p.y + 1][p.x] = 0;
				maze_data[p.y][p.x] &= ~S;
				maze_data[p.y + 1][p.x] &= ~N;
				walls.add(new Wall(p.x, p.y + 1, S));
				walls.add(new Wall(p.x, p.y + 1, E));
				walls.add(new Wall(p.x, p.y + 1, W));
			}
			if (p.dir == E && p.x + 1 < width && floors[p.y][p.x + 1] != 0) {
				floors[p.y][p.x + 1] = 0;
				maze_data[p.y][p.x] &= ~E;
				maze_data[p.y][p.x + 1] &= ~W;
				walls.add(new Wall(p.x + 1, p.y, N));
				walls.add(new Wall(p.x + 1, p.y, S));
				walls.add(new Wall(p.x + 1, p.y, E));
			}
			if (p.dir == W && p.x - 1 >= 0 && floors[p.y][p.x - 1] != 0) {
				floors[p.y][p.x - 1] = 0;
				maze_data[p.y][p.x] &= ~W;
				maze_data[p.y][p.x - 1] &= ~E;
				walls.add(new Wall(p.x - 1, p.y, N));
				walls.add(new Wall(p.x - 1, p.y, S));
				walls.add(new Wall(p.x - 1, p.y, W));
			}
		}
		return maze_data;
	}
}
