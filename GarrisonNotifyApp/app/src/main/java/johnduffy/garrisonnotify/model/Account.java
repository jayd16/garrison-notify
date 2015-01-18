package johnduffy.garrisonnotify.model;

/**
 * Created by johnduffy on 1/16/2015.
 */
public class Account {
    public String name;
    public InProgressMissionData missionData;

    public Account(String name, InProgressMissionData missionData) {
        this.name = name;
        this.missionData = missionData;
    }
}
