import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.FlowEdge;
import edu.princeton.cs.algs4.FlowNetwork;
import edu.princeton.cs.algs4.FordFulkerson;
import edu.princeton.cs.algs4.In;

public final class BaseballElimination
{
    private final int nots;
    private final int[] wins;
    private final int[] losses;
    private final int[] reamins;
    private final int[][] against;
    private final Map<String, Integer> NameToIndex = new HashMap<>();
    private final Map<Integer, String> IndexToName = new HashMap<>();

    public BaseballElimination(String filename)      // create a baseball division from given filename in format specified below
    {
        In in = new In(filename);
        nots = in.readInt();
        in.readLine();
        wins = new int[nots];
        losses = new int[nots];
        reamins = new int[nots];
        against = new int[nots][nots];
        String line;
        int count = 0;
        while ((line = in.readLine()) != null)
        {
            line = line.replaceFirst("\\s*","");
            String[] temp = line.split("\\s+");
            //set basic value
            NameToIndex.put(temp[0], count);
            IndexToName.put(count, temp[0]);
            wins[count] = Integer.parseInt(temp[1]);
            losses[count] = Integer.parseInt(temp[2]);
            reamins[count] = Integer.parseInt(temp[3]);
            //set value in against
            for (int i = 0; i < nots; i++)
            {
                if (count != i)
                {
                    against[count][i] = Integer.parseInt(temp[i + 4]);
                }
            }
            count++;
        }
    }

    public int numberOfTeams()                        // number of teams
    {
        return nots;
    }

    public Iterable<String> teams()                                // all teams
    {
        return NameToIndex.keySet();
    }

    public int wins(String team)                      // number of wins for given team
    {
        if (NameToIndex.keySet().contains(team))
        {
            return wins[NameToIndex.get(team)];
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }
    }

    public int losses(String team)                    // number of losses for given team
    {
        if (NameToIndex.keySet().contains(team))
        {
            return losses[NameToIndex.get(team)];
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }
    }

    public int remaining(String team)                 // number of remaining games for given team
    {

        if (NameToIndex.keySet().contains(team))
        {
            return reamins[NameToIndex.get(team)];
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }
    }

    public int against(String team1, String team2)    // number of remaining games between team1 and team2
    {
        if (NameToIndex.keySet().contains(team1) && NameToIndex.keySet().contains(team2))
        {
            return against[NameToIndex.get(team1)][NameToIndex.get(team2)];
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }
    }

    public boolean isEliminated(String team)              // is given team eliminated?
    {
        if (NameToIndex.keySet().contains(team))
        {
            FlowNetwork net = Network(team);
            FordFulkerson fulkerson = new FordFulkerson(net, 0, net.V() - 1);
            int max = AllGames(team);
            return max != fulkerson.value();
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }

    }

    public Iterable<String> certificateOfElimination(String team)  // subset R of teams that eliminates given team; null if not eliminated
    {
        if (NameToIndex.keySet().contains(team))
        {
            FlowNetwork net = Network(team);
            FordFulkerson fulkerson = new FordFulkerson(net, 0, net.V() - 1);
            int max = AllGames(team);
            if (max != fulkerson.value())
            {
                Iterable<String> elimination = new ArrayList<>();
                for (FlowEdge edge : net.edges())
                {
                    if (edge.from() == 0 && edge.flow() < edge.capacity())
                    {
                        int a = 0;
                        for (int current = 0; current < nots; current++)
                        {
                            for (int others = current + 1; others < nots; others++) // each pair
                            {
                                if (a == edge.to())
                                {
                                    ((ArrayList<String>) elimination).add(IndexToName.get(current));
                                    break;
                                } else
                                {
                                    a++;
                                }
                            }
                        }
                    }
                }
                return elimination;
            } else
            {
                return null;
            }
        } else
        {
            throw new java.lang.IllegalArgumentException();
        }

    }

    private FlowNetwork Network(String team)
    {
        int GivenTeam = NameToIndex.get(team);
        int teams = against.length; // number of teams(include current)
        int pairs = (int) (Math.pow(teams, 2) - teams) / 2; // number of pairs(include current)
        int maxwin = wins[GivenTeam] + reamins[GivenTeam]; // available wins for other teams
        FlowNetwork net = new FlowNetwork(pairs + teams + 2);
        int counter = 1; //
        for (int current = 0; current < teams; current++)
        {
            for (int others = current + 1; others < teams; others++) // each pair
            {
                if (current != GivenTeam && others != GivenTeam)// avoid test team
                {
                    net.addEdge(new FlowEdge(0, counter, against[current][others]));//s to paris
                    net.addEdge(new FlowEdge(counter, 1 + pairs + current, Integer.MAX_VALUE)); // pairs to result
                    net.addEdge(new FlowEdge(counter, 1 + pairs + others, Integer.MAX_VALUE)); // pairs to result
                }
                counter++; // for pair placement
            }
            if (current != GivenTeam && maxwin - wins[current] >= 0)
            {
                net.addEdge(new FlowEdge(1 + pairs + current, pairs + teams + 1, maxwin - wins[current])); // result to t
            }
        }
        return net;
    }

    private int AllGames(String name)
    {
        int GivenTeam = NameToIndex.get(name);
        int NoT = against.length;
        int games = 0;
        for (int current = 0; current < NoT; current++)
        {
            for (int others = current + 1; others < NoT; others++) // each pair
            {
                if (current != GivenTeam && others != GivenTeam)
                {
                    games += against[current][others];
                }
            }
        }
        return games;
    }

    public static void main(String[] args)
    {

        BaseballElimination division = new BaseballElimination(args[0]);

        for (String team : division.teams())
        {
            if (division.isEliminated(team))
            {
                StdOut.print(team + " is eliminated by the subset R = { ");
                for (String t : division.certificateOfElimination(team))
                {
                    StdOut.print(t + " ");
                }
                StdOut.println("}");
            } else
            {
                StdOut.println(team + " is not eliminated");
            }
        }
    }
}
