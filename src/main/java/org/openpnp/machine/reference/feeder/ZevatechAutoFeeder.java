/*
 * Copyright (C) 2011 Jason von Nieda <jason@vonnieda.org>
 * 
 * This file is part of OpenPnP.
 * 
 * OpenPnP is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * OpenPnP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with OpenPnP. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * For more information about OpenPnP visit http://openpnp.org
 */

package org.openpnp.machine.reference.feeder;

import org.openpnp.gui.support.Wizard;
import org.openpnp.machine.reference.ReferenceFeeder;
import org.openpnp.machine.reference.ReferenceNozzle;
import org.openpnp.machine.reference.ReferenceNozzleTip;
import org.openpnp.machine.reference.feeder.wizards.ReferenceAutoFeederConfigurationWizard;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Actuator;
import org.openpnp.spi.Nozzle;
import org.openpnp.spi.PropertySheetHolder;
import org.openpnp.util.MovableUtils;
import org.pmw.tinylog.Logger;
import org.simpleframework.xml.Attribute;

import javax.swing.*;

public class ZevatechAutoFeeder extends ReferenceAutoFeeder {

    @Override
    public void feed(Nozzle nozzle) throws Exception {
        if (actuatorName == null || actuatorName.equals("")) {
            Logger.warn("No actuatorName specified for feeder {}.", getName());
            return;
        }
        Actuator actuator = nozzle.getHead().getActuatorByName(actuatorName);
        if (actuator == null) {
            actuator = Configuration.get().getMachine().getActuatorByName(actuatorName);
        }
        if (actuator == null) {
            throw new Exception("Feed failed. Unable to find an actuator named " + actuatorName);
        }

        // make sure we move to where we need to pick from....
        MovableUtils.moveToLocationAtSafeZ(nozzle, getPickLocation());

        if (actuatorType == ActuatorType.Boolean) {
            actuator.actuate(actuatorValue != 0);
        }
        else {
            actuator.actuate(actuatorValue);
        }

        ReferenceNozzle refNozzle = (ReferenceNozzle) nozzle;
        Actuator VacuumSenseActuator = nozzle.getHead().getActuatorByName(refNozzle.getVacuumSenseActuatorName());
        if (actuator != null) {
            ReferenceNozzleTip nt = refNozzle.getNozzleTip();
            double vacuumLevel = Double.parseDouble(actuator.read());
            if (refNozzle.isInvertVacuumSenseLogic()) {
                if (vacuumLevel > nt.getVacuumLevelPartOn()) {
                    throw new Exception(String.format(
                            "Feed failure: Vacuum level %f is higher than expected value of %f for part on. Part may have failed to pick.",
                            vacuumLevel, nt.getVacuumLevelPartOn()));
                }
            }
            else {
                if (vacuumLevel < nt.getVacuumLevelPartOn()) {
                    throw new Exception(String.format(
                            "Feed failure: Vacuum level %f is lower than expected value of %f for part on. Part may have failed to pick.",
                            vacuumLevel, nt.getVacuumLevelPartOn()));
                }
            }
        }
    }

    @Override
    public boolean getDoesFeederDoPick() { return true; }

}
