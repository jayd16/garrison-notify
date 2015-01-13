local frame = CreateFrame("FRAME"); -- Need a frame to respond to events
frame:RegisterEvent("ADDON_LOADED"); -- Fired when saved variables are loaded
frame:RegisterEvent("PLAYER_LOGOUT"); -- Fired when about to log out 
frame:RegisterEvent("GARRISON_MISSION_STARTED");

function frame:OnEvent(event, arg1)
 if event == "GARRISON_MISSION_STARTED" then
  InProgressMissionData = SetInProgressMissions();
 end
end
frame:SetScript("OnEvent", frame.OnEvent);

function SetInProgressMissions() 
  print("Garrison Notify!");
  InProgressMissionData = {};
  local missions = table.shallow_copy(C_Garrison.GetInProgressMissions());
  for i,mission in pairs(missions) do 
    -- print(mission["missionID"], mission["description"], mission["timeLeft"])
    local hrPattern = "(%d+) hr"
    local minPattern = "(%d+) min"
    local hr = tonumber(mission["timeLeft"]:match(hrPattern))
    local min = tonumber(mission["timeLeft"]:match(minPattern))
    local timeLeftSeconds =0
    
    if hr~=nil then
      timeLeftSeconds = timeLeftSeconds + hr*3600
    end
    
    if min~=nil then
      timeLeftSeconds = timeLeftSeconds + min*60
    end

    -- print(timeLeftSeconds)
    -- print(timeLeftSeconds + time())
    mission["endTime"] = timeLeftSeconds + time()
    mission["followers"] = nil
    InProgressMissionData[tostring(mission["missionID"])] = mission
  end
  --InProgressMissionData["time"] = time()
  for k,v in pairs(InProgressMissionData) do print(k,v) end
  return InProgressMissionData
end

function table.shallow_copy(t)
  local t2 = {}
  for k,v in pairs(t) do
    t2[k] = v
  end
  return t2
end